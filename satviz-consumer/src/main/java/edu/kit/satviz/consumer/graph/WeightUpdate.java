package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeInvocationException;
import edu.kit.satviz.consumer.bindings.NativeObject;
import edu.kit.satviz.consumer.bindings.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

/**
 * An implementation of {@link GraphUpdate} that updates a set of edges by
 * adding deltas to their weights.
 */
public final class WeightUpdate implements GraphUpdate {

  private static final Struct STRUCT = Struct.builder()
      .field("index1", long.class, CLinker.C_POINTER)
      .field("index2", long.class, CLinker.C_POINTER)
      .field("weight", long.class, CLinker.C_POINTER)
      .field("n", int.class, CLinker.C_INT)
      .build();

  private static final MethodHandle SUBMIT_WEIGHT_UPDATE = NativeObject.lookupFunction(
      "submit_weight_update",
      MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER)
  );

  private final Map<Edge, Float> values = new HashMap<>();

  /**
   * Add an edge whose weight should be adjusted via this batch of updates.
   *
   * @param index1 One end of the edge
   * @param index2 The other end of the edge
   * @param weight The amount to add to the current edge weight
   */
  public void add(int index1, int index2, float weight) {
    values.put(new Edge(index1, index2), weight);
  }

  @Override
  public void submitTo(Graph graph) {
    try (ResourceScope local = ResourceScope.newConfinedScope()) {
      MemorySegment segment = toSegment(local);
      SUBMIT_WEIGHT_UPDATE.invokeExact(graph.getPointer(), segment.address());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while submitting weight update", e);
    }
  }

  private MemorySegment toSegment(ResourceScope scope) {
    MemorySegment segment = STRUCT.allocateNew(scope);
    int size = values.size();
    MemorySegment indices1 = MemorySegment.allocateNative(
        MemoryLayout.sequenceLayout(size, CLinker.C_INT), scope);
    MemorySegment indices2 = MemorySegment.allocateNative(
        MemoryLayout.sequenceLayout(size, CLinker.C_INT), scope);
    MemorySegment weights = MemorySegment.allocateNative(
        MemoryLayout.sequenceLayout(size, CLinker.C_FLOAT), scope);
    STRUCT.varHandle("n").set(segment, size);
    STRUCT.varHandle("index1").set(segment, indices1.address().toRawLongValue());
    STRUCT.varHandle("index2").set(segment, indices2.address().toRawLongValue());
    STRUCT.varHandle("weight").set(segment, weights.address().toRawLongValue());
    long intSize = CLinker.C_INT.byteSize();
    long floatSize = CLinker.C_FLOAT.byteSize();
    int index = 0;
    for (var entry : values.entrySet()) {
      Edge edge = entry.getKey();
      MemoryAccess.setIntAtOffset(indices1, index * intSize, edge.index1());
      MemoryAccess.setIntAtOffset(indices2, index * intSize, edge.index2());
      MemoryAccess.setFloatAtOffset(weights, index * floatSize, entry.getValue());
      index++;
    }
    return segment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WeightUpdate that = (WeightUpdate) o;
    return Objects.equals(values, that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(values);
  }
}
