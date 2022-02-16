package edu.kit.satviz.consumer.bindings;

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
