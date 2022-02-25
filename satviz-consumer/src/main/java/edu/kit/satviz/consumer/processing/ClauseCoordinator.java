package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.SerializationException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class that manages incoming {@link ClauseUpdate}s. A {@code ClauseCoordinator}
 * <ul>
 *   <li>stores clause updates sequentially in an {@link ExternalClauseBuffer}</li>
 *   <li>manages snapshots of {@link ClauseUpdateProcessor}s and {@link Graph}s</li>
 *   <li>calls registered {@link ClauseUpdateProcessor}s when visualization is advanced</li>
 *   <li>holds a cursor pointing to the most recently processed clause update</li>
 * </ul>
 *
 * <p><strong>Snapshots</strong> can be created via {@link #takeSnapshot()}, which will serialise
 * the current state of the graph and the currently registered {@link ClauseUpdateProcessor}s
 * to a temporary file. When {@link #seekToUpdate(long)} is used, the snapshot closest to the
 * desired index will be deserialised and loaded.
 *
 * <p>Instances of this class hold closeable resources. It should therefore be ensured that
 * {@code ClauseCoordinator}s are {@link #close() closed} after usage.
 *
 * <p>This class is thread safe.
 */
public class ClauseCoordinator implements AutoCloseable {

  private final Path tempDir;
  private final Path snapshotDir;
  private final TreeMap<Long, Snapshot> snapshots;
  private final List<ClauseUpdateProcessor> processors;
  private final Graph graph;
  private final ExternalClauseBuffer buffer;

  // snapshotLock provides mutual exclusion for snapshot creation and loading
  private final ReentrantLock snapshotLock;
  // stateLock provides mutual exclusion and consistency for operations that modify currentUpdate
  private final ReentrantLock stateLock;
  // processorLock provides mutual exclusion for addProcessor and takeSnapshot to coordinate
  // change detection in the list of processors. it is not needed to access the list elsewhere.
  private final Lock processorLock;

  // currentUpdate is volatile, even though the stateLock prevents concurrent modification already.
  // this is because while updates to currentUpdate need to be consistent and coordinated,
  // concurrent reads are legal. to ensure a consistent and up-to-date view of currentUpdate, it is
  // therefore marked volatile.
  private volatile long currentUpdate;
  private Runnable changeListener;

  private final int variableAmount;

  /**
   * Create a new {@code ClauseCoordinator}.
   *
   * @param graph The underlying graph
   * @param tempDir The directory where the internal files used by this class will be stored.
   * @param variableAmount The amount of variables in the corresponding SAT instance.
   * @throws IOException If there is an I/O error while setting up the internal files
   */
  public ClauseCoordinator(Graph graph, Path tempDir, int variableAmount) throws IOException {
    this.graph = graph;
    this.tempDir = tempDir;
    this.variableAmount = variableAmount;

    this.snapshotDir = Files.createTempDirectory(tempDir, "satviz-snapshots");
    snapshotDir.toFile().deleteOnExit();
    this.snapshots = new TreeMap<>();
    this.processors = new CopyOnWriteArrayList<>();
    this.changeListener = () -> {
    };
    this.currentUpdate = 0;
    this.buffer = new ExternalClauseBuffer(tempDir);
    this.snapshotLock = new ReentrantLock();
    this.stateLock = new ReentrantLock();
    this.processorLock = new ReentrantLock();
    // so the initial snapshot has something to compare the processor list against
    this.snapshots.put(0L, new Snapshot(null, new ClauseUpdateProcessor[0]));
    // take initial snapshot to have a baseline in the snapshots TreeMap
    takeSnapshot();
  }

  /**
   * Add a {@link ClauseUpdateProcessor} to the list of processors that will work on clause updates.
   *
   * @param processor The processor to add.
   */
  public void addProcessor(ClauseUpdateProcessor processor) {
    processorLock.lock();
    try {
      processors.add(processor);
    } finally {
      processorLock.unlock();
    }
  }

  /**
   * Advances the internal cursor by {@code numUpdates} clause updates, calling the registered
   * processors in the process.
   *
   * <p>If {@code numUpdates} is higher than the amount of currently remaining updates available,
   * this method will advance the highest amount of updates possible.
   *
   * @param numUpdates The amount of updates to process. This is a no-op if {@code numUpdates < 1}.
   * @throws IOException If an I/O error occurs.
   * @throws SerializationException If the updates can't be deserialised from the external buffer.
   *                                This may only happen as a result of external interference with
   *                                the internal files used by this object.
   */
  public void advanceVisualization(int numUpdates)
      throws IOException, SerializationException {
    // to prevent alien call processor.process from looping back
    if (stateLock.isHeldByCurrentThread()) {
      return;
    }
    advance(numUpdates);
    changeListener.run();
  }

  /**
   * Returns the cursor of this coordinator.
   *
   * @return The index of the update up to which this coordinator has advanced.
   */
  public long currentUpdate() {
    return currentUpdate;
  }

  /**
   * The total amount of clause updates added to this coordinator.
   *
   * @return how many updates have been added via {@link #addClauseUpdate(ClauseUpdate)}.
   */
  public long totalUpdateCount() {
    return buffer.size();
  }

  /**
   * Resets the internal cursor to an update at a specific index.
   *
   * <p>This will do the following:
   * <ol>
   *   <li>Find the snapshot that is closest to, but not newer than {@code index}</li>
   *   <li>Load the snapshot, i.e. deserialise the {@link Graph} state and:
   *   <ul>
   *     <li>{@link ClauseUpdateProcessor#deserialize(InputStream) Deserialise} the processors
   *     that were registered at the time of the snapshot</li>
   *     <li>{@link ClauseUpdateProcessor#reset() Reset} the processors that were <em>not</em>
   *     registered at the time of the snapshot but that <em>are</em> registered <em>now</em></li>
   *   </ul>
   *   </li>
   *   <li>Advance the remaining number of clause updates to get to the desired {@code index},
   *   as per {@link #advanceVisualization(int)}</li>
   * </ol>
   *
   * @param index The index of the clause update to seek to.
   * @throws IOException If an I/O error occurs
   * @throws SerializationException If deserialisation fails. This can only happen realistically
   *                                if the internal files of this processor have been subject to
   *                                external interference.
   */
  public void seekToUpdate(long index) throws IOException, SerializationException {
    if (index < 0) {
      throw new IllegalArgumentException("Index must not be negative: " + index);
    }
    // to prevent alien calls from looping back
    if (stateLock.isHeldByCurrentThread()) {
      return;
    }

    stateLock.lock();
    try {
      long closestSnapshotIndex = loadClosestSnapshot(index);
      currentUpdate = closestSnapshotIndex;
      advance((int) (index - closestSnapshotIndex));
    } finally {
      stateLock.unlock();
    }
    changeListener.run();
  }

  private void advance(int numUpdates) throws SerializationException, IOException {
    if (numUpdates < 1 || currentUpdate == buffer.size()) {
      return;
    }

    // the state needs to be locked to synchronise advancements.
    // snapshots need to be locked to prevent snapshot creation of half-updated graphs
    snapshotLock.lock();
    stateLock.lock();
    try {
      ClauseUpdate[] updates = buffer.getClauseUpdates(currentUpdate, numUpdates);
      for (ClauseUpdateProcessor processor : processors) {
        graph.submitUpdate(processor.process(updates, graph));
      }
      // this operation is not atomic although currentUpdate is volatile.
      // However, this is no problem because write access to currentUpdate is always coordinated
      // using stateLock.
      currentUpdate += updates.length;
    } finally {
      stateLock.unlock();
      snapshotLock.unlock();
    }
  }

  /**
   * Take a snapshot at the {@link #currentUpdate() current update}.<br>
   * This will serialise the underlying {@link Graph} and registered {@link ClauseUpdateProcessor}s.
   *
   * @throws IOException If an I/O error occurs
   */
  public void takeSnapshot() throws IOException {
    // prevent alien calls from looping back
    if (snapshotLock.isHeldByCurrentThread()) {
      return;
    }

    // snapshots are locked to prevent overlapping snapshot creation and to synchronise access
    // to the snapshots TreeMap
    snapshotLock.lock();
    // processors are locked so that updates happen either before or after taking the snapshot
    processorLock.lock();
    try {
      long current = currentUpdate();
      Path snapshotFile = Files.createTempFile(snapshotDir, "snapshot", null);
      snapshotFile.toFile().deleteOnExit();
      try (var stream = new BufferedOutputStream(Files.newOutputStream(snapshotFile))) {
        for (ClauseUpdateProcessor processor : processors) {
          processor.serialize(stream);
        }
        graph.serialize(stream);
      }

      // if the processor list has changed since the last snapshot, create a new snapshot array.
      // otherwise, reuse the processors snapshot from the most recent snapshot
      ClauseUpdateProcessor[] mostRecentProcessors = snapshots.floorEntry(current)
          .getValue().processors();
      ClauseUpdateProcessor[] processorsSnapshot =
          Arrays.asList(mostRecentProcessors).equals(processors)
              ? mostRecentProcessors
              : this.processors.toArray(new ClauseUpdateProcessor[0]);
      snapshots.put(current, new Snapshot(snapshotFile, processorsSnapshot));
    } finally {
      processorLock.unlock();
      snapshotLock.unlock();
    }

  }

  /**
   * Append a clause update to this coordinator.
   *
   * @param clauseUpdate the update to add
   * @throws IOException if an I/O error occurs
   * @throws IllegalArgumentException if the given clause update is
   *                                  not valid for the underlying SAT instance
   */
  public void addClauseUpdate(ClauseUpdate clauseUpdate) throws IOException {
    if (isValidClauseUpdate(clauseUpdate)) {
      buffer.addClauseUpdate(clauseUpdate);
      changeListener.run();
    } else {
      throw new IllegalArgumentException(clauseUpdate + " is invalid.");
    }
  }

  /**
   * Register a {@code Runnable} that will be called whenever one of the following values change:
   * <ul>
   *   <li>{@link #currentUpdate()}</li>
   *   <li>{@link #totalUpdateCount()}</li>
   * </ul>.
   *
   * <p><strong>Note:</strong> If the above criteria is met, the listener is called, but not
   * necessarily the other way around (the listener being called does not imply that something
   * has changed).
   *
   * @param action The action to run.
   */
  public void registerChangeListener(Runnable action) {
    changeListener = Objects.requireNonNull(action);
  }

  private long loadClosestSnapshot(long index) throws IOException, SerializationException {
    // snapshots need to be locked - we don't want to create a snapshot while in the middle of
    // restoring some previous state.
    snapshotLock.lock();
    try {
      // find nearest snapshot to index
      Map.Entry<Long, Snapshot> entry = snapshots.floorEntry(index);
      if (index >= currentUpdate && entry.getKey() <= currentUpdate) {
        return currentUpdate;
      }
      try {
        Snapshot snapshot = entry.getValue();
        List<ClauseUpdateProcessor> snapshotProcessors = Arrays.asList(snapshot.processors());

        try (var stream = new BufferedInputStream(Files.newInputStream(snapshot.file()))) {
          // lock state to ensure consistent graph and processor views for advance()
          stateLock.lock();
          // restore processor and graph state
          for (ClauseUpdateProcessor processor : snapshotProcessors) {
            processor.deserialize(stream);
          }
          graph.deserialize(stream);
        }

        // lock processors so processor updates don't interleave
        processorLock.lock();
        List<ClauseUpdateProcessor> nonSnapshotProcessors = new ArrayList<>(this.processors);
        nonSnapshotProcessors.removeAll(snapshotProcessors);

        // reset processors that were added later
        for (ClauseUpdateProcessor processor : nonSnapshotProcessors) {
          processor.reset();
        }

        List<ClauseUpdateProcessor> newProcessors = new ArrayList<>();
        newProcessors.addAll(snapshotProcessors); // first the processors that exist in the snapshot
        newProcessors.addAll(nonSnapshotProcessors); // then the ones that were added afterwards

        // set new processor list
        this.processors.clear();
        this.processors.addAll(newProcessors);
        return entry.getKey();
      } finally {
        stateLock.unlock();
        processorLock.unlock();
      }
    } finally {
      snapshotLock.unlock();
    }
  }

  private boolean isValidClauseUpdate(ClauseUpdate update) {
    int[] literals = update.clause().literals();
    for (int i = 0; i < literals.length; i++) {
      if (Math.abs(literals[i]) > variableAmount || literals[i] == 0) {
        return false;
      }
      for (int j = i + 1; j < literals.length; j++) {
        if (Math.abs(literals[i]) == Math.abs(literals[j])) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void close() throws IOException {
    buffer.close();
    // delete tempDir
    Files.walkFileTree(tempDir, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc != null) {
          throw exc;
        }
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private record Snapshot(Path file, ClauseUpdateProcessor[] processors) {

  }

}
