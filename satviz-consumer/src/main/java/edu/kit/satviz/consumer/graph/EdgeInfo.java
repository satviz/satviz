package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeObject;
import edu.kit.satviz.consumer.bindings.Struct;
import java.util.Objects;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

/**
 * A class that holds information about an edge in a {@link Graph}.
 *
 * <p>{@link #close()} should be called on instances of this class backed by native memory.
 */
public class EdgeInfo extends NativeObject {

  public static final Struct STRUCT = Struct.builder()
      .field("index1", int.class, CLinker.C_INT)
      .field("index2", int.class, CLinker.C_INT)
      .field("weight", float.class, CLinker.C_FLOAT)
      .build();

  private final Edge edge;
  private final float weight;

  /**
   * Create an {@code EdgeInfo} object from a {@code MemorySegment} containing a {@code EdgeInfo}
   * C struct.
   *
   * @param segment the piece of memory holding the {@code EdgeInfo} struct.
   */
  public EdgeInfo(MemorySegment segment) {
    super(segment.address());
    int index1 = (int) STRUCT.varHandle("index1").get(segment);
    int index2 = (int) STRUCT.varHandle("index2").get(segment);
    this.edge = new Edge(index1, index2);
    this.weight = (float) STRUCT.varHandle("weight").get(segment);
  }

  /**
   * Create an {@code EdgeInfo} object that is not backed by native memory.
   *
   * @param edge the {@link Edge}.
   * @param weight the weight associated with the edge.
   */
  public EdgeInfo(Edge edge, float weight) {
    super(MemoryAddress.NULL);
    this.edge = edge;
    this.weight = weight;
  }

  /**
   * Returns the edge described by this object.
   *
   * @return an {@link Edge} record.
   */
  public Edge getEdge() {
    return edge;
  }

  /**
   * Returns the weight of this edge.
   *
   * @return The weight.
   */
  public float getWeight() {
    return weight;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EdgeInfo edgeInfo = (EdgeInfo) o;
    return Objects.equals(edgeInfo.edge, edge)
        && Float.compare(edgeInfo.weight, weight) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(edge, weight);
  }

  /**
   * Frees the native memory associated with this object.
   */
  @Override
  public void close() {
    CLinker.freeMemory(getPointer());
  }
}
