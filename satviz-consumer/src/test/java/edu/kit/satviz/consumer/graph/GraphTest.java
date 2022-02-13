package edu.kit.satviz.consumer.graph;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/* it is not in the scope of this test to do rigorous checks on the functionality of the
   graph methods. Rather, its purpose is mostly to check if the bindings work at all. */
class GraphTest {

  private Graph graph;

  @BeforeEach
  void setUp() {
    graph = Graph.create(3);
  }

  @AfterEach
  void tearDown() {
    graph.close();
  }

  @Test
  void test_queryNode_existingNode() {
    var nodeInfo = graph.queryNode(1);
    assertEquals(new NodeInfo(1, 0, 0, 0), nodeInfo);
  }

  @Test
  void test_submitUpdate_weightUpdate() {
    var update = new WeightUpdate();
    update.add(0, 1, 1.0f);
    graph.submitUpdate(update);
    var edge = graph.queryEdge(0, 1);
    assertEquals(new EdgeInfo(new Edge(0, 1), 1), edge);
  }

  @Test
  void test_submitUpdate_heatUpdate() {
    var update = new HeatUpdate();
    update.add(0, 1.0f);
    graph.submitUpdate(update);
    var node = graph.queryNode(0);
    assertEquals(new NodeInfo(0, 0xff, 0, 0), node);
  }

  @Test
  void test_recalculateLayout() {
    assertDoesNotThrow(() -> graph.recalculateLayout());
  }

  @Test
  void test_adaptLayout() {
    assertDoesNotThrow(() -> graph.adaptLayout());
  }

  @Test
  void test_serialize() {
    var output = new ByteArrayOutputStream();
    graph.serialize(output);
    assertTrue(output.toByteArray().length > 0);
  }

}
