package edu.kit.satviz.common;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a statement about subjects of type T that can be validated.
 *
 * <p>This class may be used to check input argument conditions for example.
 *
 * @param <T> The type of this constraint's subject.
 * @see #validate(Object)
 */
@FunctionalInterface
public interface Constraint<T> {

  /**
   * Validates this constraint.<br>
   * If the constraint is fulfilled for the input {@code obj}, nothing happens.
   * If {@code obj} violates the constraint, a {@link ConstraintValidationException} is thrown.
   *
   * @param obj The input object to apply this constraint to.
   * @throws ConstraintValidationException If the object violates the constraint in some way.
   */
  void validate(T obj) throws ConstraintValidationException;

  /**
   * Returns a Constraint that first applies the given {@code mapper} function
   * to the input and then validates this constraint.
   *
   * @param mapper A function U -> T that will be applied to the input of the new constraint.
   * @param <U> The type of the new constraint's subjects.
   * @return A new constraint that validates this constraint over {@code mapper}.
   */
  default <U> Constraint<U> on(Function<U, T> mapper) {
    return obj -> validate(mapper.apply(obj));
  }

  /**
   * A static factory method to produce a constraint that simply checks the given {@code predicate}
   * and fails with the given {@code message} if it is not {@code true} for its input.
   *
   * @param predicate A predicate function, representing a condition for an object of type T
   * @param message An error message that will be used in the {@link ConstraintValidationException}
   *                if validation fails
   * @param <T> The subject type of the constraint
   * @return A constraint that checks a predicate.
   */
  static <T> Constraint<T> checking(Predicate<T> predicate, String message) {
    return obj -> {
      if (!predicate.test(obj)) {
        throw new ConstraintValidationException(message);
      }
    };
  }

  /**
   * Throws a {@link ConstraintValidationException} with the given message.
   *
   * @param message The message for the exception.
   * @throws ConstraintValidationException always.
   */
  default void fail(String message) throws ConstraintValidationException {
    throw new ConstraintValidationException(message);
  }

  /**
   * Produces a constraint that combines all given constraints. In other words, the resulting
   * constraint is the logical <em>AND</em> of the given constraints.
   *
   * <p>The new constraint will validate the given constraints in order and fail immediately
   * with the provided exception when one of them fails.
   *
   * @param constraints The constraints to combine
   * @param <T> The type of the constraints
   * @return A constraint representing "constraint1 && constraint2 && ..."
   */
  @SafeVarargs
  static <T> Constraint<T> allOf(Constraint<? super T>... constraints) {
    return obj -> {
      for (Constraint<? super T> constraint : constraints) {
        constraint.validate(obj);
      }
    };
  }

}
