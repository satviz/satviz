package edu.kit.satviz.serial;

import java.nio.ByteBuffer;

/**
 * A class for deserializing objects in several steps.
 * The total amount of bytes required to deserialize an object can be passed by parts.
 * This way, the deserialization process can be halted and resumed.
 *
 * @param <T> the type of object to deserialize
 * @author luwae
 */
public abstract class SerialBuilder<T> {
  /**
   * Adds a single byte to the deserialization process.
   *
   * @param i the byte (as integer, only lowest 8 bits are used)
   * @return whether the deserialization process is complete
   * @throws SerializationException if the byte was invalid
   */
  public abstract boolean addByte(int i) throws SerializationException;

  /**
   * Adds a number of bytes to the deserialization process.
   * Read from the buffer as long as bytes are remaining and the object is not finished.
   *
   * @param bb the buffer to read from
   * @return whether the deserialization process is complete
   * @throws SerializationException if one of the bytes read was invalid
   */
  public boolean addBytes(ByteBuffer bb) throws SerializationException {
    // naive; @override for more efficient implementations
    while (bb.hasRemaining()) {
      if (addByte(bb.get())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the state of the deserialization process.
   *
   * @return whether the process is complete or not.
   */
  public abstract boolean objectFinished();

  /**
   * Gets the finished object.
   * <code>null</code> as return value does not necessarily indicate that the process
   * is not done. Some builders might see <code>null</code> as a valid object.
   *
   * @return the object if done, <code>null</code> otherwise.
   */
  public abstract T getObject();
}
