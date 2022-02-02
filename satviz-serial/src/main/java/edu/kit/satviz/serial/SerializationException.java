package edu.kit.satviz.serial;

/**
 * An exception that is thrown whenever an object cannot be serialized or deserialized
 *     from the given data.
 */
public class SerializationException extends Exception {
  /**
   * Creates a new exception with specified error message.
   *
   * @param msg the message
   */
  public SerializationException(String msg) {
    super(msg);
  }
}
