package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeObject;
import jdk.incubator.foreign.MemoryAddress;

public class EdgeInfo extends NativeObject {

  private final Edge edge;
  private final float weight;

  public EdgeInfo(Edge edge, float weight) {
    super(MemoryAddress.NULL);
    this.edge = edge;
    this.weight = weight;
  }

  public Edge getEdge() {
    return edge;
  }

  public float getWeight() {
    return weight;
  }

  @Override
  public void close() {

  }
}
