package edu.kit.satviz.consumer.bindings;

/**
 * Thrown to indicate that there was an error while calling a native function.
 */
public class NativeInvocationException extends RuntimeException {

  public NativeInvocationException(String message) {
    super(message);
  }

  public NativeInvocationException(String message, Throwable cause) {
    super(message, cause);
  }

  public NativeInvocationException(Throwable cause) {
    super(cause);
  }
}
