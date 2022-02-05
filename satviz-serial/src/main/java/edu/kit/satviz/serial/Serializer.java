package edu.kit.satviz.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An abstract class for serializing and deserializing objects.
 *
 * @param <T> the type of object that gets serialized
 */
public abstract class Serializer<T> {

  /**
   * Serializes an object of type <code>T</code>.
   * If the object cannot be serialized (i.e., {@link SerializationException} is thrown),
   *     the output stream shall be left unwritten.
   *
   * @param t the object
   * @param out the stream to write to
   * @throws IOException if the stream cannot be used
   * @throws SerializationException if the object cannot be serialized
   */
  public abstract void serialize(T t, OutputStream out) throws IOException, SerializationException;

  /**
   * Serializes an object.
   * Tries to cast the object to type <code>T</code>, and serialize that.
   *
   * @implNote The suppression of type safety violation warnings is intentional, as we cannot
   *     guarantee at compile time that a user of this method will always use it properly.
   *     However, a checked exception will be thrown at runtime if a cast is not possible.
   * @param o the object
   * @param out the stream to write to
   * @throws IOException if the stream cannot be used
   * @throws ClassCastException if the object cannot be cast to type <code>T</code>
   * @throws SerializationException if the object cannot be serialized
   */
  public void serializeUnsafe(Object o, OutputStream out) throws IOException,
      ClassCastException, SerializationException {
    @SuppressWarnings("unchecked")
    T t = (T) o;
    serialize(t, out);
  }

  /**
   * Deserializes an object of type <code>T</code>.
   * The default implementation uses a corresponding builder to construct the object.
   *
   * @param in the stream to read from
   * @return the object that was constructed
   * @throws IOException if the stream cannot be used
   * @throws SerializationException if the stream contains invalid data
   */
  public T deserialize(InputStream in) throws IOException, SerializationException {
    SerialBuilder<T> builder = getBuilder();
    if (builder == null) {
      throw new NullPointerException("no builder available");
    }

    int i;
    do {
      i = in.read();
      if (i == -1) {
        throw new SerializationException("unexpected end of stream");
      }
    } while (!builder.addByte((byte) i));

    return builder.getObject();
  }

  /**
   * Gets a new {@link SerialBuilder} to deserialize an object of type <code>T</code> in steps.
   *
   * @return new builder
   */
  public abstract SerialBuilder<T> getBuilder();
}
