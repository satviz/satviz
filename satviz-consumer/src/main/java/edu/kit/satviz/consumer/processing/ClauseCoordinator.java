package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class ClauseCoordinator implements AutoCloseable {

  private final Path tempDir;
  private final TreeMap<Long, Path> snapshots;
  private final List<ClauseUpdateProcessor> processors;
  private final Graph graph;


  private Runnable changeListener;
  private long currentUpdate;

  private ExternalClauseBuffer buffer;

  public ClauseCoordinator(Graph graph, Path tempDir) throws IOException {
    this.tempDir = tempDir;
    this.snapshots = new TreeMap<>();
    this.processors = new ArrayList<>();
    this.changeListener = () -> {};
    this.currentUpdate = 0;
    this.graph = graph;
    this.buffer = new ExternalClauseBuffer(tempDir);
  }

  public void addProcessor(ClauseUpdateProcessor processor) {
    processors.add(processor);
  }

  public void advanceVisualization(int numUpdates) throws IOException, SerializationException {
    ClauseUpdate[] updates = buffer.getClauseUpdates(currentUpdate, numUpdates);
    for (ClauseUpdateProcessor processor : processors) {
      processor.process(updates, graph);
    }
    changeListener.run();
  }

  public long currentUpdate() {
    return currentUpdate;
  }

  public void seekToUpdate(long index) {
    currentUpdate = index;
  }

  public void takeSnapshot() {

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
