package edu.kit.satviz.consumer.bindings;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import jdk.incubator.foreign.*;

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

  private static final MethodHandle SUBMIT_WEIGHT_UPDATE = lookupFunction(
      "submit_weight_update",
      MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER)
  );

  private static final MethodHandle SUBMIT_HEAT_UPDATE = lookupFunction(
      "submit_heat_update",
      MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER)
  );

  private static final MethodHandle RECALCULATE_LAYOUT = lookupFunction(
      "recalculate_layout",
      MethodType.methodType(void.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER)
  );

  private static final MethodHandle ADAPT_LAYOUT = lookupFunction(
      "adapt_layout",
      MethodType.methodType(void.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER)
  );

  private static final MethodHandle SERIALIZE = lookupFunction(
      "serialize",
      MethodType.methodType(MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER)
  );

  private static final MethodHandle DESERIALIZE = lookupFunction(
      "deserialize",
      MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER)
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

  public void recalculateLayout() {
    try {
      RECALCULATE_LAYOUT.invokeExact(pointer);
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while recalculating layout", e);
    }
  }

  public void adaptLayout() {
    try {
      ADAPT_LAYOUT.invokeExact(pointer);
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while adapting layout", e);
    }
  }

  public String serialize() {
    try {
      return CLinker.toJavaString((MemoryAddress) SERIALIZE.invokeExact(pointer));
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while serializing graph", e);
    }
  }

  public void deserialize(String string) {
    try (ResourceScope local = ResourceScope.newConfinedScope()) {
      DESERIALIZE.invokeExact(pointer, CLinker.toCString(string, local));
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while deserializing graph representation", e);
    }

  }

  @Override
  public void close() {
    try {
      RELEASE_GRAPH.invokeExact(pointer);
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while releasing graph", e);
    }
  }
}
