package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.SerializationException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClauseCoordinatorTest {

  private static final int VARIABLE_AMOUNT = 10;

  private Path tempDir;
  private ClauseCoordinator coordinator;

  private Graph graph = mock(Graph.class);

  private ClauseUpdateProcessor processor1 = mock(ClauseUpdateProcessor.class);

  private ClauseUpdateProcessor processor2 = mock(ClauseUpdateProcessor.class);

  private final ClauseUpdate[] clauseUpdates = new ClauseUpdate[]{
          new ClauseUpdate(new Clause(new int[]{6, 1}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{6, 2}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{6, 3}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{-6, 4}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{-6, 5}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{1, 4}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{2, 4}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{3, 4}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{1, 5}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{2, 5}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{3, 5}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{6}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{}), ClauseUpdate.Type.ADD)
  };

  @BeforeEach
  void setUp() throws IOException {
    tempDir = Files.createDirectory(Paths.get("src/test"));
    coordinator = new ClauseCoordinator(graph, tempDir);
    coordinator.addProcessor(processor1);
    coordinator.addProcessor(processor2);
  }

  @Test
  void advanceVisualization() throws IOException, SerializationException {
    for (int i = 0; i < 4; i++) {
      coordinator.addClauseUpdate(clauseUpdates[i]);
    }
    assertEquals(0, coordinator.currentUpdate());
    coordinator.advanceVisualization(3);
    assertEquals(3, coordinator.currentUpdate());
    for (int i = 4; i < 8; i++) {
      coordinator.addClauseUpdate(clauseUpdates[i]);
    }
    assertEquals(3, coordinator.currentUpdate());
    coordinator.advanceVisualization(3);
    assertEquals(6, coordinator.currentUpdate());
    for (int i = 8; i < 12; i++) {
      coordinator.addClauseUpdate(clauseUpdates[i]);
    }
    assertEquals(6, coordinator.currentUpdate());
    coordinator.advanceVisualization(3);
    assertEquals(9, coordinator.currentUpdate());
    for (int i = 12; i < 13; i++) {
      coordinator.addClauseUpdate(clauseUpdates[i]);
    }
    assertEquals(9, coordinator.currentUpdate());
    coordinator.advanceVisualization(3);
    assertEquals(12, coordinator.currentUpdate());
    coordinator.advanceVisualization(3);
    assertEquals(13, coordinator.currentUpdate());
  }

  @AfterEach
  void clean() throws IOException {
    coordinator.close();
  }

}