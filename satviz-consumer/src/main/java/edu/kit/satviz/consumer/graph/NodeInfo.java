package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.Struct;
import java.util.Objects;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemorySegment;

/**
 * A class that holds information about a node in a {@link Graph}.
 */
public final class NodeInfo {

  public static final Struct STRUCT = Struct.builder()
      .field("index", int.class, CLinker.C_INT)
      .field("heat", int.class, CLinker.C_INT)
      .field("x", float.class, CLinker.C_FLOAT)
      .field("y", float.class, CLinker.C_FLOAT)
      .build();

  private final int index;
  private final int heat;
  private final float x;
  private final float y;

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
    this.index = (int) STRUCT.varHandle("index").get(segment);
    this.heat = (int) STRUCT.varHandle("heat").get(segment);
    this.x = (float) STRUCT.varHandle("x").get(segment);
    this.y = (float) STRUCT.varHandle("y").get(segment);
  }

  /**
   * Create a {@code NodeInfo} object that is not backed by native memory.
   *
   * @param index The index of the node described.
   * @param heat The heat value of the node.
   * @param x The node's x position
   * @param y The node's y position
   */
  public NodeInfo(int index, int heat, float x, float y) {
    this.index = index;
    this.heat = heat;
    this.x = x;
    this.y = y;
  }

  /**
   * Returns the index of the node.
   *
   * @return the index
   */
  public int getIndex() {
    return index;
  }

  /**
   * Returns the heat value of the node.
   *
   * @return a number from 0-255.
   */
  public int getHeat() {
    return heat;
  }

  /**
   * Returns the current x position of the node.
   *
   * @return the x position
   */
  public float getX() {
    return x;
  }

  /**
   * Returns the current y position of the node.
   *
   * @return the y position
   */
  public float getY() {
    return y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeInfo nodeInfo = (NodeInfo) o;
    return index == nodeInfo.index
        && heat == nodeInfo.heat
        && Float.compare(nodeInfo.x, x) == 0
        && Float.compare(nodeInfo.y, y) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, heat, x, y);
  }

}
