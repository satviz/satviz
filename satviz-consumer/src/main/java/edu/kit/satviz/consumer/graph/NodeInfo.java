package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeObject;
import jdk.incubator.foreign.MemoryAddress;

public class NodeInfo extends NativeObject {

  private final int index;
  private final int heat;
  private final float x;
  private final float y;

  public NodeInfo(int index, int heat, float x, float y) {
    super(MemoryAddress.NULL);
    this.index = index;
    this.heat = heat;
    this.x = x;
    this.y = y;
  }

  public int getIndex() {
    return index;
  }

  public int getHeat() {
    return heat;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  @Override
  public void close() {

  }
}
