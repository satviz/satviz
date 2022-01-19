package edu.kit.satviz.consumer.config;

import java.util.function.DoubleUnaryOperator;

public enum WeightFactor {

  CONSTANT(n -> 1),
  RECIPROCAL(n -> 1.0 / n),
  EXPONENTIAL(n -> 1.0 / Math.pow(2, n));

  private final DoubleUnaryOperator function;

  WeightFactor(DoubleUnaryOperator function) {
    this.function = function;
  }

  public double apply(int n) {
    return function.applyAsDouble(n);
  }
}