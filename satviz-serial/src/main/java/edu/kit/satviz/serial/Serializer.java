package edu.kit.satviz.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A class for (de)serializing objects.
 *
 * @param <T> the type of object that gets serialized
 * @author luwae
 */
public abstract class Serializer<T> {

  /**
   * Serializes an object of type <code>T</code>.
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
   *
   * @param in the stream to read from
   * @return the object that was constructed
   * @throws IOException if the stream cannot be used
   * @throws SerializationException if the stream contains invalid data
   */
  public abstract T deserialize(InputStream in) throws IOException, SerializationException;

  /**
   * Gets a new {@link SerialBuilder} to deserialize an object of type <code>T</code> in steps.
   *
   * @return new builder
   */
  public abstract SerialBuilder<T> getBuilder();
}
