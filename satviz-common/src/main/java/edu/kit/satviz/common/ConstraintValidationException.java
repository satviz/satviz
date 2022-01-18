package edu.kit.satviz.common;

public class ConstraintValidationException extends Exception {

  public ConstraintValidationException(String message) {
    super("Failed to validate constraint: " + message);
  }

  public ConstraintValidationException(String message, Throwable cause) {
    super("Failed to validate constraint: " + message, cause);
  }
}
