package edu.kit.satviz.consumer.bindings;

/**
 * Thrown to indicate there was an error initialising the native components.
 * This is non-recoverable.
 */
public class NativeInitializationError extends Error {

  public NativeInitializationError(String message, Throwable cause) {
    super(message, cause);
  }
}
