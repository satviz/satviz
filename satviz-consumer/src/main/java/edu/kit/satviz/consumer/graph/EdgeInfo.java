package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeObject;
import jdk.incubator.foreign.MemoryAddress;

public class EdgeInfo extends NativeObject {

  private final int index1;
  private final int index2;
  private final float weight;

  public EdgeInfo(int index1, int index2, float weight) {
    super(MemoryAddress.NULL);
    this.index1 = index1;
    this.index2 = index2;
    this.weight = weight;
  }

  public int getIndex1() {
    return index1;
  }

  public int getIndex2() {
    return index2;
  }

  public float getWeight() {
    return weight;
  }

  @Override
  public void close() {

  }
}
