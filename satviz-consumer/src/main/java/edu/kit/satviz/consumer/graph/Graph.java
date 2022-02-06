package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeInvocationException;
import edu.kit.satviz.consumer.bindings.NativeObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.ResourceScope;

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

  // TODO move these to WeightUpdate/HeatUpdate
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
      RECALCULATE_LAYOUT.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while recalculating layout", e);
    }
  }

  public void adaptLayout() {
    try {
      ADAPT_LAYOUT.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while adapting layout", e);
    }
  }

  public void serialize(OutputStream stream) {
    try {
      String s = CLinker.toJavaString((MemoryAddress) SERIALIZE.invokeExact(getPointer()));
      stream.write(s.getBytes(StandardCharsets.UTF_8));
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while serializing graph", e);
    }
  }

  public void deserialize(InputStream stream) {
    try (ResourceScope local = ResourceScope.newConfinedScope()) {
      // TODO make sure we're actually supposed to read the entire stream
      String string = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      DESERIALIZE.invokeExact(getPointer(), CLinker.toCString(string, local));
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while deserializing graph representation", e);
    }

  }

  public void destroy() {
    try {
      RELEASE_GRAPH.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while releasing graph", e);
    }
  }

  @Override
  public void close() {
    destroy();
  }
}
