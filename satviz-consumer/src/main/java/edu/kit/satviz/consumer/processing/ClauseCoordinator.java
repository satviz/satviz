package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.sat.ClauseUpdate;

import java.io.File;
import java.nio.file.Path;
import java.util.TreeMap;

public class ClauseCoordinator {

  private Path tempDir;
  private TreeMap<Long, Path> snapshots;
  private File clauseFile;

  public ClauseCoordinator(Graph graph, Path tempDir) {
    this.tempDir = tempDir;
  }

  public void addProcessor(ClauseUpdateProcessor processor) {

  }

  public void advanceVisualization(int numUpdates) {

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

  }

  public void registerChangeListener(Runnable action) {

  }

  private void loadSnapshot(long index, Path snapshot) {

  }

}
