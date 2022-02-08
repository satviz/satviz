package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeObject;
import edu.kit.satviz.consumer.bindings.Struct;
import java.util.Objects;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

public final class NodeInfo extends NativeObject {

  public static final Struct STRUCT = Struct.builder()
      .field("index", int.class, CLinker.C_INT)
      .field("heat", int.class, CLinker.C_INT)
      .field("x", float.class, CLinker.C_FLOAT)
      .field("y", float.class, CLinker.C_FLOAT)
      .build();

  private final int index;
  private final int heat;
  private final float x;
  private final float y;

  public NodeInfo(MemorySegment segment) {
    super(segment.address());
    this.index = (int) STRUCT.varHandle("index").get(segment);
    this.heat = (int) STRUCT.varHandle("heat").get(segment);
    this.x = (float) STRUCT.varHandle("x").get(segment);
    this.y = (float) STRUCT.varHandle("y").get(segment);
  }

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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeInfo nodeInfo = (NodeInfo) o;
    return index == nodeInfo.index
        && heat == nodeInfo.heat
        && Float.compare(nodeInfo.x, x) == 0
        && Float.compare(nodeInfo.y, y) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, heat, x, y);
  }

  @Override
  public void close() {
    CLinker.freeMemory(getPointer());
  }
}
