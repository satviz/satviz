package edu.kit.satviz.common;

import java.util.function.Function;

public interface Constraint<T> {

  void validate(T obj) throws ConstraintValidationException;

  default <U> Constraint<U> on(Function<U, T> mapper) {
    return (obj) -> validate(mapper.apply(obj));
  }

  default void fail(String message) throws ConstraintValidationException {
    throw new ConstraintValidationException(message);
  }

  @SafeVarargs
  static <T> Constraint<T> allOf(Constraint<T>... constraints) {
    return (obj) -> {
      for (Constraint<T> constraint : constraints) {
        constraint.validate(obj);
      }
    };
  }

}
