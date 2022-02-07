package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.sat.ClauseUpdate;

import edu.kit.satviz.serial.ClauseSerialBuilder;
import edu.kit.satviz.serial.ClauseSerializer;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ClauseCoordinator {

  private final Path tempDir;
  private final TreeMap<Long, Path> snapshots;
  private final List<ClauseUpdateProcessor> processors;
  private final Graph graph;
  private final RandomAccessFile clauseFile;
  private final OutputStream clauseStream;

  private long updateCounter;
  private long currentUpdate;

  public ClauseCoordinator(Graph graph, Path tempDir) {
    this.tempDir = tempDir;
    this.snapshots = new TreeMap<>();
    this.processors = new ArrayList<>();
    this.updateCounter = 0;
    this.graph = graph;
    this.clauseFile = null;
    this.clauseStream = null;
  }

  public void addProcessor(ClauseUpdateProcessor processor) {
    processors.add(processor);
  }

  public void advanceVisualization(int numUpdates) {
    ClauseUpdate[] updates = null; // TODO: get correct updates from file
    for (ClauseUpdateProcessor processor : processors) {
      processor.process(updates, graph);
    }
  }

  public long currentUpdate() {
    return 0;
  }

  public long numberOfUpdates() {
    return 0;
  }

  public void seekToUpdate(long index) {

  }

  public void takeSnapshot() {

  }

  public void addClauseUpdate(ClauseUpdate clauseUpdate) {
    updateCounter++;
    // TODO: Save Clause in clauseFile
  }

  public void registerChangeListener(Runnable action) {

  }

  private void loadSnapshot(long index, Path snapshot) {

  }

}
