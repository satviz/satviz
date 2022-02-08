package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeInvocationException;
import edu.kit.satviz.consumer.bindings.NativeObject;
import edu.kit.satviz.consumer.bindings.Struct;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

public final class WeightUpdate implements GraphUpdate {

  private static final Struct STRUCT = Struct.builder()
      .field("n", int.class, CLinker.C_INT)
      .field("index1", MemoryAddress.class, CLinker.C_POINTER)
      .field("index2", MemoryAddress.class, CLinker.C_POINTER)
      .field("weight", MemoryAddress.class, CLinker.C_POINTER)
      .build();

  private static final MethodHandle SUBMIT_WEIGHT_UPDATE = NativeObject.lookupFunction(
      "submit_weight_update",
      MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER)
  );

  private final Map<Edge, Float> values = new HashMap<>();

  public void add(int index1, int index2, float weight) {
    values.put(new Edge(index1, index2), weight);
  }

  @Override
  public void submitTo(Graph graph) {
    try (ResourceScope local = ResourceScope.newConfinedScope()) {
      MemorySegment segment = toSegment(local);
      SUBMIT_WEIGHT_UPDATE.invokeExact(graph.getPointer(), segment.address());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while submitting weight update");
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
    STRUCT.varHandle("index1").set(indices1);
    STRUCT.varHandle("index2").set(indices2);
    STRUCT.varHandle("weight").set(weights);
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

}
