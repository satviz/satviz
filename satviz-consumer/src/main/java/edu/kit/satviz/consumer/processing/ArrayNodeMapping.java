package edu.kit.satviz.consumer.processing;

import java.util.function.IntUnaryOperator;

/**
 * A node mapping backed by an array.
 */
public class ArrayNodeMapping implements IntUnaryOperator {

  private final int[] array;

  /**
   * Creates a new array-backed node mapping.
   *
   * @param array the array, mapping each variable (0-indexed) to a node index.
   */
  public ArrayNodeMapping(int[] array) {
    this.array = array;
  }

  @Override
  public int applyAsInt(int literal) {
    return array[Math.abs(literal) - 1];
  }

}
