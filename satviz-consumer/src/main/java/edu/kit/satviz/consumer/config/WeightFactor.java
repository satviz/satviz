package edu.kit.satviz.consumer.config;

import java.util.function.DoubleUnaryOperator;

/**
 * This enum contains the possible weight factors, by which each clause
 * affects the corresponding edge weights in the graph.
 */
public enum WeightFactor {

  /**
   * Scales the weight-update by <code>1</code>.<br><br>
   *
   * <code>apply(n) = 1</code>
   */
  CONSTANT(n -> 1),

  /**
   * Scales the weight-update by <code>1/n</code>,
   * with <code>n</code> being the clause-size.<br><br>
   *
   * <code>apply(n) = 1/n</code>
   */
  RECIPROCAL(n -> 1.0 / n),

  /**
   * Scales the weight-update by <code>1/(2^n)</code>,
   * with <code>n</code> being the clause-size.<br><br>
   *
   * <code>apply(n) = 1/(2^n)</code>
   */
  EXPONENTIAL(n -> 1.0 / Math.pow(2, n));

  private final DoubleUnaryOperator function;

  WeightFactor(DoubleUnaryOperator function) {
    this.function = function;
  }

  /**
   * This method calculates the factor, by which single clauses affect edge weights
   * of their corresponding edges in the graph.
   *
   * @param n The clause-size.
   * @return The calculated amount of increase for the edge weights.
   */
  public double apply(int n) {
    return function.applyAsDouble(n);
  }

  /**
   * This method calculates the factor, by which single clauses affect edge weights
   * of their corresponding edges in the graph.<br>
   * The {@code min} parameter determines the (exclusive) boundary, within which the return value
   * is rounded down to {@code 0}.<br>
   *<p>
   * Example:<br>
   * {@code >> factor = WeightFactor.RECIPROCAL; n = 4;}<br>
   * {@code >> factor.apply(n);}<br>
   * {@code 0.25}<br>
   * {@code >> factor.apply(n, 0.25);}<br>
   * {@code 0.25}<br>
   * {@code >> factor.apply(n, 0.3);}<br>
   * {@code 0.0}<br><br>
   *</p>
   * <i>NOTE: This function is meant to prevent underflow issues.</i>
   *
   * @param n The clause-size.
   * @param min The minimal value.
   * @return The calculated amount of increase for the edge weights.
   */
  public double apply(int n, double min) {
    double result = apply(n);
    return (result >= min) ? result : 0;
  }

}