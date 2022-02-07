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
import jdk.incubator.foreign.MemorySegment;
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

  private static final MethodHandle QUERY_NODE = lookupFunction(
      "query_node",
      MethodType.methodType(MemorySegment.class, MemoryAddress.class, int.class),
      FunctionDescriptor.of(NodeInfo.LAYOUT, CLinker.C_POINTER, CLinker.C_INT)
  );

  private static final MethodHandle QUERY_EDGE = lookupFunction(
      "query_edge",
      MethodType.methodType(MemorySegment.class, MemoryAddress.class, int.class, int.class),
      FunctionDescriptor.of(EdgeInfo.LAYOUT, CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT)
  );

  private Graph(MemoryAddress pointer) {
    super(pointer);
  }

  public static Graph create(long nodes) {
    if (nodes < 0) {
      throw new IllegalArgumentException("Graph must have a non-negative amount of nodes");
    }
    try {
      return new Graph((MemoryAddress) NEW_GRAPH.invokeExact(nodes));
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while creating graph", e);
    }
  }

  public void submitUpdate(GraphUpdate update) {
    update.submitTo(this);
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

  public NodeInfo queryNode(int index) {
    try {
      MemorySegment segment = (MemorySegment) QUERY_NODE.invokeExact(getPointer(), index);
      return new NodeInfo(segment);
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while querying node", e);
    }
  }

  public EdgeInfo queryEdge(int index1, int index2) {
    try {
      MemorySegment segment = (MemorySegment) QUERY_EDGE.invokeExact(getPointer(), index1, index2);
      return new EdgeInfo(segment);
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while querying edge", e);
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
