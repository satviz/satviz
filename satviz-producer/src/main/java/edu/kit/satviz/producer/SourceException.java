package edu.kit.satviz.producer;

public class SourceException extends Exception {

  public SourceException(String message) {
    super(message);
  }

  public SourceException(String message, Throwable cause) {
    super(message, cause);
  }

  public SourceException(Throwable cause) {
    super(cause);
  }
}
