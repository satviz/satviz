package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeObject;
import java.lang.invoke.VarHandle;
import jdk.incubator.foreign.CLinker;
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

  private final int index1;
  private final int index2;
  private final float weight;

  public EdgeInfo(MemorySegment segment) {
    super(segment.address());
    this.index1 = (int) INDEX1_HANDLE.get(segment);
    this.index2 = (int) INDEX2_HANDLE.get(segment);
    this.weight = (float) WEIGHT_HANDLE.get(segment);
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
    CLinker.freeMemory(getPointer());
  }
}
