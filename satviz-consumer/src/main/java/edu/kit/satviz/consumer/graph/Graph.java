package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeInvocationException;
import edu.kit.satviz.consumer.bindings.NativeObject;
import edu.kit.satviz.consumer.bindings.Struct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;

/**
 * The graph model used by satviz. Every instance of this class is bound to a
 * {@code satviz::graph::Graph} instance in C++.
 */
public class Graph extends NativeObject {

  public record Contraction(int remainingNodes, int[] mapping) {}

  private static final Struct SERIALIZED_DATA = Struct.builder()
      .field("data", long.class, CLinker.C_POINTER)
      .field("n", long.class, CLinker.C_LONG)
      .build();

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
      MethodType.methodType(MemorySegment.class, MemoryAddress.class),
      FunctionDescriptor.of(SERIALIZED_DATA.getLayout(), CLinker.C_POINTER)
  );

  private static final MethodHandle DESERIALIZE = lookupFunction(
      "deserialize",
      MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class, long.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_LONG)
  );

  private static final MethodHandle NUM_NODES = lookupFunction(
      "num_nodes",
      MethodType.methodType(int.class, MemoryAddress.class),
      FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER)
  );

  private static final MethodHandle QUERY_NODE = lookupFunction(
      "query_node",
      MethodType.methodType(MemorySegment.class, MemoryAddress.class, int.class),
      FunctionDescriptor.of(NodeInfo.STRUCT.getLayout(), CLinker.C_POINTER, CLinker.C_INT)
  );

  private static final MethodHandle QUERY_EDGE = lookupFunction(
      "query_edge",
      MethodType.methodType(MemorySegment.class, MemoryAddress.class, int.class, int.class),
      FunctionDescriptor.of(EdgeInfo.STRUCT.getLayout(),
          CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT)
  );

  private static final MethodHandle COMPUTE_CONTRACTION = lookupFunction(
      "compute_contraction",
      MethodType.methodType(int.class, MemoryAddress.class, int.class, MemoryAddress.class),
      FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_INT, CLinker.C_POINTER)
  );

  // Only protected because of mockup
  protected Graph(MemoryAddress pointer) {
    super(pointer);
  }

  /**
   * Initialize a graph with the given, fixed amount of nodes.
   *
   * @param nodes The amount of nodes/vertices. Must be non-negative.
   * @return a new {@code Graph} without any edges.
   */
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

  /**
   * Submit a {@link GraphUpdate} to this graph.
   *
   * @param update The update to submit.
   * @implNote {@code update.submitTo(this);}
   */
  public void submitUpdate(GraphUpdate update) {
    update.submitTo(this);
  }

  /**
   * Recalculate the graph layout.
   */
  public void recalculateLayout() {
    try {
      RECALCULATE_LAYOUT.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while recalculating layout", e);
    }
  }

  /**
   * Adapt the graph layout.
   */
  public void adaptLayout() {
    try {
      ADAPT_LAYOUT.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while adapting layout", e);
    }
  }

  /**
   * Serialize this graph to an {@code OutputStream}.
   *
   * @param stream The {@code OutputStream} this graph's data should be written to.
   * @throws IOException if an I/O problem occurs
   * @see #deserialize(InputStream)
   */
  public void serialize(OutputStream stream) throws IOException {
    try (var localScope = ResourceScope.newConfinedScope()) {
      var segment = (MemorySegment) SERIALIZE.invokeExact(
          SegmentAllocator.ofScope(localScope), getPointer());
      long n = (long) SERIALIZED_DATA.varHandle("n").get(segment);
      MemoryAddress data = MemoryAddress.ofLong(
          (long) SERIALIZED_DATA.varHandle("data").get(segment));
      byte[] byteData = data.asSegment(n, localScope).toByteArray();
      stream.write(byteData);
      CLinker.freeMemory(data);
    } catch (IOException e) {
      throw e;
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while serializing graph", e);
    }
  }

  /**
   * Deserialize a graph from an {@code InputStream} and store its data in this instance.
   *
   * @param stream The stream the graph data should be read from.
   * @throws IOException if an I/O problem occurs
   * @see #serialize(OutputStream)
   */
  public void deserialize(InputStream stream) throws IOException {
    try (var localScope = ResourceScope.newConfinedScope()) {
      byte[] byteData = stream.readAllBytes();
      // TODO: 25/02/2022 figure out if this is a memory leak
      MemorySegment nativeBytes = MemorySegment.allocateNative(byteData.length, localScope);
      nativeBytes.copyFrom(MemorySegment.ofArray(byteData));
      DESERIALIZE.invokeExact(getPointer(), nativeBytes.address(), (long) byteData.length);
    } catch (IOException e) {
      throw e;
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while deserializing graph representation", e);
    }

  }

  /**
   * Query how many nodes there are in the graph.
   *
   * @return the number of nodes.
   */
  public int numNodes() {
    try {
      return (int) NUM_NODES.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while querying number of nodes", e);
    }
  }

  /**
   * Retrieve information about a node in this graph.
   *
   * @param index The index of the node to find.
   * @return A {@link NodeInfo} object detailing information about the node.
   */
  public NodeInfo queryNode(int index) {
    try (var localScope = ResourceScope.newConfinedScope()) {
      MemorySegment segment = (MemorySegment) QUERY_NODE.invokeExact(
          SegmentAllocator.ofScope(localScope), getPointer(), index);
      return new NodeInfo(segment);
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while querying node", e);
    }
  }

  /**
   * Retrieve information about an edge in this graph.
   *
   * @param index1 The first end of the edge, a node index.
   * @param index2 The second end of the edge, a node index.
   * @return A {@link EdgeInfo} object detailing information about the edge.
   */
  public EdgeInfo queryEdge(int index1, int index2) {
    try (var localScope = ResourceScope.newConfinedScope()) {
      MemorySegment segment = (MemorySegment) QUERY_EDGE.invokeExact(
          SegmentAllocator.ofScope(localScope), getPointer(), index1, index2);
      return new EdgeInfo(segment);
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while querying edge", e);
    }
  }

  public Contraction computeContraction(int iterations) {
    try (var localScope = ResourceScope.newConfinedScope()) {
      var segment = MemorySegment.allocateNative(CLinker.C_INT.byteSize() * numNodes(), localScope);
      int remainingNodes = (int) COMPUTE_CONTRACTION.invokeExact(getPointer(), iterations, segment.address());
      int[] mapping = segment.toIntArray();
      return new Contraction(remainingNodes, mapping);
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while computing graph contraction", e);
    }
  }

  /**
   * Delete the underlying native object.<br>
   * To clean up {@code Graph} instances, you should generally use {@link #close()} instead.
   */
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
