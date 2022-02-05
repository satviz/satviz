package edu.kit.satviz.consumer.bindings;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;

public class Graph extends NativeObject {

  private static final MethodHandle NEW_GRAPH = lookupFunction(
      "new_graph",
      MethodType.methodType(MemoryAddress.class, long.class),
      FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_LONG)
  );

  private static final MethodHandle RELEASE_GRAPH = lookupFunction(
      "release_graph",
      MethodType.methodType(void.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER)
  );


  private Graph(MemoryAddress pointer) {
    super(pointer);
  }

  public static Graph create(long nodes) {
    try {
      return new Graph((MemoryAddress) NEW_GRAPH.invokeExact(nodes));
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while creating graph", e);
    }
  }

  @Override
  public void close() {
    try {
      RELEASE_GRAPH.invokeExact();
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while releasing graph", e);
    }
  }
}
