package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeObject;
import edu.kit.satviz.consumer.bindings.Struct;
import java.util.Objects;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

public class EdgeInfo extends NativeObject {

  public static final Struct STRUCT = Struct.builder()
      .field("index1", int.class, CLinker.C_INT)
      .field("index2", int.class, CLinker.C_INT)
      .field("weight", float.class, CLinker.C_FLOAT)
      .build();

  private final Edge edge;
  private final float weight;

  public EdgeInfo(MemorySegment segment) {
    super(segment.address());
    int index1 = (int) STRUCT.varHandle("index1").get(segment);
    int index2 = (int) STRUCT.varHandle("index2").get(segment);
    this.edge = new Edge(index1, index2);
    this.weight = (float) STRUCT.varHandle("weight").get(segment);
  }

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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EdgeInfo edgeInfo = (EdgeInfo) o;
    return Objects.equals(edgeInfo.edge, edge)
        && Float.compare(edgeInfo.weight, weight) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(edge, weight);
  }

  @Override
  public void close() {
    CLinker.freeMemory(getPointer());
  }
}
