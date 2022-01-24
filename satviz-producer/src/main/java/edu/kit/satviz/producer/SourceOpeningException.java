package edu.kit.satviz.producer;

public class SourceOpeningException extends Exception {

  public SourceOpeningException(String message) {
    super(message);
  }

  public SourceOpeningException(String message, Throwable cause) {
    super(message, cause);
  }

  public SourceOpeningException(Throwable cause) {
    super(cause);
  }
}
