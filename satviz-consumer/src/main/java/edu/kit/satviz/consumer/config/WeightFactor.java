package edu.kit.satviz.consumer.config;

import java.util.function.DoubleUnaryOperator;

/**
 * This enum contains the possible weight factors, by which each clause
 * affects the corresponding edge weights in the graph.
 *
 * @author johnnyjayjay
 */
public enum WeightFactor {

  /**
   * Each new clause adds <code>1</code> to the corresponding edge weights.<br><br>
   *
   * <code>apply(n) = 1</code>
   */
  CONSTANT(n -> 1),

  /**
   * Each new clause adds <code>1/n</code> to the corresponding edge weights,
   * with <code>n</code> being the clause-size.<br><br>
   *
   * <code>apply(n) = 1/n</code>
   */
  RECIPROCAL(n -> 1.0 / n),

  /**
   * Each new clause adds <code>1/(2^n)</code> to the corresponding edge weights,
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
}