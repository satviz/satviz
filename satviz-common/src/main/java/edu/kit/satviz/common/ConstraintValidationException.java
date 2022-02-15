package edu.kit.satviz.common;

/**
 * Thrown to indicate that a {@link Constraint} validation failed.
 *
 * @see Constraint
 */
public class ConstraintValidationException extends Exception {

  /**
   * Same as {@link Exception#Exception(String)}, but with a message prefix.
   *
   * @param message The exception message.
   */
  public ConstraintValidationException(String message) {
    super("Failed to validate constraint: " + message);
  }

  /**
   * Same as {@link Exception#Exception(String, Throwable)}, but with a message prefix.
   *
   * @param message The exception message.
   * @param cause The cause of the exception.
   */
  public ConstraintValidationException(String message, Throwable cause) {
    super("Failed to validate constraint: " + message, cause);
  }
}
