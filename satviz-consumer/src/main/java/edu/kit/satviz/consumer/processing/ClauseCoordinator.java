package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.SerializationException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

public class ClauseCoordinator implements AutoCloseable {

  private final Path tempDir;
  private final TreeMap<Long, Path> snapshots;
  private final List<ClauseUpdateProcessor> processors;
  private final Graph graph;
  private final AtomicLong currentUpdate;
  private final ExternalClauseBuffer buffer;

  private Runnable changeListener;

  public ClauseCoordinator(Graph graph, Path tempDir) throws IOException {
    this.tempDir = tempDir;
    this.snapshots = new TreeMap<>();
    this.processors = new ArrayList<>();
    this.changeListener = () -> {
    };
    this.currentUpdate = new AtomicLong(0);
    this.graph = graph;
    this.buffer = new ExternalClauseBuffer(tempDir);
    takeSnapshot();
  }

  public void addProcessor(ClauseUpdateProcessor processor) {
    processors.add(processor);
  }

  public synchronized void advanceVisualization(int numUpdates)
      throws IOException, SerializationException {
    if (numUpdates < 1) {
      return;
    }
    ClauseUpdate[] updates = buffer.getClauseUpdates(currentUpdate.get(), numUpdates);
    for (ClauseUpdateProcessor processor : processors) {
      processor.process(updates, graph);
    }
    currentUpdate.addAndGet(updates.length);
    changeListener.run();
  }

  public long currentUpdate() {
    return currentUpdate.get();
  }

  public synchronized void seekToUpdate(long index) throws IOException, SerializationException {
    if (index < 0) {
      throw new IllegalArgumentException("Index must not be negative: " + index);
    }
    long closestSnapshotIndex = loadClosestSnapshot(index);
    currentUpdate.set(closestSnapshotIndex);
    advanceVisualization((int) (index - closestSnapshotIndex));
  }

  public void takeSnapshot() throws IOException {
    long current = currentUpdate();
    synchronized (snapshots) {
      Path snapshotFile = Files.createTempFile(tempDir, "satviz-snapshot", null);
      try (var stream = new BufferedOutputStream(Files.newOutputStream(snapshotFile))) {
        for (ClauseUpdateProcessor processor : processors) {
          processor.serialize(stream);
        }
        graph.serialize(stream);
      }
      snapshots.put(current, snapshotFile);
    }
  }

  public void addClauseUpdate(ClauseUpdate clauseUpdate) throws IOException {
    buffer.addClauseUpdate(clauseUpdate);
  }

  public void registerChangeListener(Runnable action) {
    changeListener = Objects.requireNonNull(action);
  }

  private long loadClosestSnapshot(long index) throws IOException {
    synchronized (snapshots) {
      Map.Entry<Long, Path> entry = snapshots.floorEntry(index);
      Path snapshot = entry.getValue();
      try (var stream = new BufferedInputStream(Files.newInputStream(snapshot))) {
        for (ClauseUpdateProcessor processor : processors) {
          processor.deserialize(stream);
        }
        graph.deserialize(stream);
      }
      return entry.getKey();
    }
  }

  @Override
  public void close() throws IOException {
    buffer.close();
  }

}
