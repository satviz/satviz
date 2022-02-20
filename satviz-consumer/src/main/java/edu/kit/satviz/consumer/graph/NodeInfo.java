package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.Struct;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemorySegment;

/**
 * A class that holds information about a node in a {@link Graph}.
 *
 * @param index The index of the node described.
 * @param heat The heat value of the node.
 * @param x The node's x position
 * @param y The node's y position
 */
public record NodeInfo(int index, int heat, float x, float y) {

  public static final Struct STRUCT = Struct.builder()
      .field("index", int.class, CLinker.C_INT)
      .field("heat", int.class, CLinker.C_INT)
      .field("x", float.class, CLinker.C_FLOAT)
      .field("y", float.class, CLinker.C_FLOAT)
      .build();

  /**
   * Create a {@code NodeInfo} object from a {@code MemorySegment} containing a {@code NodeInfo}
   * C struct.
   *
   * @apiNote The resulting object does not take ownership of the given segment,
   *          it only reads it to initialise its fields. It is therefore still the
   *          caller's responsibility to close the segment appropriately.
   * @param segment the piece of memory holding the {@code NodeInfo} struct.
   */
  public NodeInfo(MemorySegment segment) {
    this(
        (int) STRUCT.varHandle("index").get(segment),
        (int) STRUCT.varHandle("heat").get(segment),
        (float) STRUCT.varHandle("x").get(segment),
        (float) STRUCT.varHandle("y").get(segment)
    );
  }
}
