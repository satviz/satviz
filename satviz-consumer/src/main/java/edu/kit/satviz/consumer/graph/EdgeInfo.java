package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeObject;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.MemorySegment;

public class EdgeInfo extends NativeObject {

  public static final MemoryLayout LAYOUT = paddedStruct(
      CLinker.C_INT.withName("index1"),
      CLinker.C_INT.withName("index2"),
      CLinker.C_FLOAT.withName("weight")
  );

  private static final VarHandle INDEX1_HANDLE = LAYOUT.varHandle(int.class,
      PathElement.groupElement("index1"));
  private static final VarHandle INDEX2_HANDLE = LAYOUT.varHandle(int.class,
      PathElement.groupElement("index2"));
  private static final VarHandle WEIGHT_HANDLE = LAYOUT.varHandle(float.class,
      PathElement.groupElement("weight"));

  private final Edge edge;
  private final float weight;

  public EdgeInfo(MemorySegment segment) {
    super(segment.address());
    int index1 = (int) INDEX1_HANDLE.get(segment);
    int index2 = (int) INDEX2_HANDLE.get(segment);
    this.edge = new Edge(index1, index2);
    this.weight = (float) WEIGHT_HANDLE.get(segment);
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
