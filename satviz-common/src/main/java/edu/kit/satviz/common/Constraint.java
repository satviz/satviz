package edu.kit.satviz.common;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Constraint<T> {

  void validate(T obj) throws ConstraintValidationException;

  default <U> Constraint<U> on(Function<U, T> mapper) {
    return obj -> validate(mapper.apply(obj));
  }

  static <T> Constraint<T> checking(Predicate<T> predicate, String message) {
    return obj -> {
      if (!predicate.test(obj)) {
        throw new ConstraintValidationException(message);
      }
    };
  }

  default void fail(String message) throws ConstraintValidationException {
    throw new ConstraintValidationException(message);
  }

  @SafeVarargs
  static <T> Constraint<T> allOf(Constraint<T>... constraints) {
    return obj -> {
      for (Constraint<T> constraint : constraints) {
        constraint.validate(obj);
      }
    };
  }

}
