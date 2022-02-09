package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.GraphUpdate;
import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.consumer.graph.WeightUpdate;
import edu.kit.satviz.consumer.processing.mockups.MockupGraph;
import edu.kit.satviz.consumer.processing.mockups.MockupProcessor;
import edu.kit.satviz.consumer.processing.mockups.MockupUpdate;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.SerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClauseCoordinatorTest {

  private static final int VARIABLE_AMOUNT = 10;
  private static final Path tempDir = (new File("src/test/resources/temp")).toPath();

  private MockupGraph graph;
  private GraphUpdate update;
  private ClauseCoordinator coordinator;
  private List<ClauseUpdateProcessor> processors;
  private ClauseUpdate[] clauseUpdates = new ClauseUpdate[]{
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
    processors = new ArrayList<>();
    graph = MockupGraph.create(VARIABLE_AMOUNT);
    coordinator = new ClauseCoordinator(graph, tempDir);
  }

  @Test
  void advanceVisualization() throws IOException, SerializationException {
    /**for (int i = 0; i < 3; i++) {
      coordinator.addClauseUpdate(clauseUpdates[i]);
    }
    coordinator.advanceVisualization(3);**/
    processors.add(new Heatmap());
    process(Arrays.copyOfRange(clauseUpdates, 0, 2));
    process(Arrays.copyOfRange(clauseUpdates, 2, 3));
    process(Arrays.copyOfRange(clauseUpdates, 3, 5));
    processors.add(new VariableInteractionGraph());
    process(Arrays.copyOfRange(clauseUpdates, 5, 15));
    ByteArrayOutputStream  out = new ByteArrayOutputStream();
    graph.serialize(out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    graph.deserialize(in);
    Map<String, Integer> counter = graph.getUpdateCounter();
    String heatmapName = Heatmap.class.getName();
    String vigName = VariableInteractionGraph.class.getName();
    assertEquals(4, counter.get(HeatUpdate.class.getName()));
    assertEquals(1, counter.get(WeightUpdate.class.getName()));
  }

  private void process(ClauseUpdate[] array) {
    for (ClauseUpdateProcessor processor : processors) {
      graph.submitUpdate(processor.process(array, graph));
    }
  }

  @Test
  void currentUpdate() {
  }

  @Test
  void seekToUpdate() {
  }

  @Test
  void takeSnapshot() {
  }

  @Test
  void addClauseUpdate() {
  }

  @Test
  void registerChangeListener() {
  }

}