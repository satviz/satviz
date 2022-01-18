package edu.kit.satviz.common;

import java.util.function.Function;

public interface Constraint<T> {

  void validate(T obj) throws ConstraintValidationException;

  default <U> Constraint<T> on(Function<U, T> mapper) {
    return null;
  }

  default void fail(String message) {

  }

  @SafeVarargs
  static <T> Constraint<T> allOf(Constraint<T>... constraints) {
    return null;
  }

}
