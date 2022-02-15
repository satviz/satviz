package edu.kit.satviz.consumer.graph;

/**
 * An undirected edge in a {@link Graph}.
 *
 * @param index1 One end of the edge (node index)
 * @param index2 The other end of the edge (node index)
 */
public record Edge(int index1, int index2) {
}
