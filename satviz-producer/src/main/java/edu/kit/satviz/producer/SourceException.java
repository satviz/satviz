package edu.kit.satviz.producer;

/**
 * Thrown to indicate that something relating to a {@link ClauseSource} went wrong.
 *
 * <p>It may occur while {@link ClauseSource#open() opening} a source or while it is producing
 * clause updates.
 */
public class SourceException extends Exception {

  /**
   * Delegates to {@link Exception#Exception(String)}.
   *
   * @param message The exception message.
   */
  public SourceException(String message) {
    super(message);
  }

  /**
   * Delegates to {@link Exception#Exception(String, Throwable)}.
   *
   * @param message The exception message.
   * @param cause The cause of the exception.
   */
  public SourceException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Delegates to {@link Exception#Exception(Throwable)}.
   *
   * @param cause The cause of the exception.
   */
  public SourceException(Throwable cause) {
    super(cause);
  }
}
