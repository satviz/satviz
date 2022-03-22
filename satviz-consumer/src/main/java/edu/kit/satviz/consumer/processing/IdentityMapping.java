package edu.kit.satviz.consumer.processing;

import java.util.function.IntUnaryOperator;

/**
 * A node mapping that simply maps literals to their 0-indexed variable/node.
 */
public class IdentityMapping implements IntUnaryOperator {

  public static final IdentityMapping INSTANCE = new IdentityMapping();

  private IdentityMapping() {}

  @Override
  public int applyAsInt(int operand) {
    return Math.abs(operand) - 1;
  }
}
