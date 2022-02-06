package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeObject;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;

public class HeatUpdate implements GraphUpdate {

  private static final MemoryLayout LAYOUT = NativeObject.paddedStruct(
      CLinker.C_INT.withName("n"),
      CLinker.C_POINTER.withName("index"),
      CLinker.C_POINTER.withName("heat")
  );

  private static final MethodHandle SUBMIT_HEAT_UPDATE = NativeObject.lookupFunction(
      "submit_heat_update",
      MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER)
  );

  public void add(int index, int heat) {

  }

  @Override
  public void submitTo(Graph graph) {

  }
}