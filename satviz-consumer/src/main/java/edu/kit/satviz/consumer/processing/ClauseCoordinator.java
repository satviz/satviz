package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.Serializer;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

public class ClauseCoordinator implements AutoCloseable {

  private final Path tempDir;
  private final TreeMap<Long, Path> snapshots;
  private final List<ClauseUpdateProcessor> processors;
  private final Graph graph;

  private Runnable changeListener;
  private AtomicLong currentUpdate;
  private ExternalClauseBuffer buffer;

  public ClauseCoordinator(Graph graph, Path tempDir) throws IOException {
    this.tempDir = tempDir;
    this.snapshots = new TreeMap<>();
    this.processors = new ArrayList<>();
    this.changeListener = () -> {};
    this.currentUpdate = new AtomicLong(0);
    this.graph = graph;
    this.buffer = new ExternalClauseBuffer(tempDir);
  }

  public void addProcessor(ClauseUpdateProcessor processor) {
    processors.add(processor);
  }

  public synchronized void advanceVisualization(int numUpdates)
      throws IOException, SerializationException {
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

  public synchronized void seekToUpdate(long index) {
    currentUpdate.set(index);
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

  private void loadSnapshot(long index, Path snapshot) {

  }

  @Override
  public void close() throws IOException {
    buffer.close();
  }
}
