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

public class HeatUpdate implements GraphUpdate {

  private static final Struct STRUCT = Struct.builder()
      .field("n", int.class, CLinker.C_INT)
      .field("index", MemoryAddress.class, CLinker.C_POINTER)
      .field("heat", MemoryAddress.class, CLinker.C_POINTER)
      .build();

  private static final MethodHandle SUBMIT_HEAT_UPDATE = NativeObject.lookupFunction(
      "submit_heat_update",
      MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER)
  );

  private final Map<Integer, Integer> values = new HashMap<>();

  public void add(int index, int heat) {
    values.put(index, heat);
  }

  @Override
  public void submitTo(Graph graph) {
    try (ResourceScope local = ResourceScope.newConfinedScope()) {
      MemorySegment segment = toSegment(local);
      SUBMIT_HEAT_UPDATE.invokeExact(graph.getPointer(), segment.address());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while submitting heat update", e);
    }
  }

  private MemorySegment toSegment(ResourceScope scope) {
    MemorySegment segment = STRUCT.allocateNew(scope);
    int size = values.size();
    MemorySegment indices = MemorySegment.allocateNative(
        MemoryLayout.sequenceLayout(size, CLinker.C_INT), scope);
    MemorySegment heatValues = MemorySegment.allocateNative(
        MemoryLayout.sequenceLayout(size, CLinker.C_INT), scope);
    STRUCT.varHandle("n").set(segment, size);
    STRUCT.varHandle("index").set(segment, indices.address());
    STRUCT.varHandle("heat").set(segment, heatValues.address());
    long intSize = CLinker.C_INT.byteSize();
    int index = 0;
    for (var entry : values.entrySet()) {
      long offset = index * intSize;
      MemoryAccess.setIntAtOffset(indices, offset, entry.getValue());
      MemoryAccess.setIntAtOffset(heatValues, offset, entry.getValue());
      index++;
    }
    return segment;
  }
}
