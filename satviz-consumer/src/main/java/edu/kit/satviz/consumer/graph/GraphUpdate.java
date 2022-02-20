package edu.kit.satviz.consumer.graph;

/**
 * A description of structural change that can be applied to a {@link Graph}.
 */
public interface GraphUpdate {

  /**
   * Applies this update to the given graph.
   *
   * @param graph The graph.
   */
  void submitTo(Graph graph);

}
