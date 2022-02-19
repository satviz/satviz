package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.Struct;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemorySegment;

/**
 * A class that holds information about an edge in a {@link Graph}.
 *
 * @param edge the {@link Edge}.
 * @param weight the weight associated with the edge.
 */
public record EdgeInfo(Edge edge, float weight) {

  public static final Struct STRUCT = Struct.builder()
      .field("index1", int.class, CLinker.C_INT)
      .field("index2", int.class, CLinker.C_INT)
      .field("weight", float.class, CLinker.C_FLOAT)
      .build();

  /**
   * Create an {@code EdgeInfo} object from a {@code MemorySegment} containing a {@code EdgeInfo}
   * C struct.
   *
   * @apiNote The resulting object does not take ownership of the given segment,
   *          it only reads it to initialise its fields. It is therefore still the
   *          caller's responsibility to close the segment appropriately.
   * @param segment the piece of memory holding the {@code EdgeInfo} struct.
   */
  public EdgeInfo(MemorySegment segment) {
    this(
        new Edge((int) STRUCT.varHandle("index1").get(segment),
            (int) STRUCT.varHandle("index2").get(segment)),
        (float) STRUCT.varHandle("weight").get(segment)
    );
  }
}
