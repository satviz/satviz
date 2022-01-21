package edu.kit.satviz.serial;

/**
 * An exception that is thrown whenever a byte stream cannot be (de)serialized to a valid object.
 *
 * @author luwae
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
