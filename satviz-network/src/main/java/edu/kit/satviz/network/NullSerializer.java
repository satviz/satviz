package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.Serializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link Serializer} for non-existent objects (<code>null</code>).
 */
public class NullSerializer extends Serializer<Object> {

  /**
   * Serializes a <code>null</code> object.
   * Writes a single null-byte.
   *
   * @param o ignored
   * @param out the stream to write to
   * @throws IOException if the stream can't be used
   */
  @Override
  public void serialize(Object o, OutputStream out) throws IOException, SerializationException {
    if (o != null) {
      throw new SerializationException("can only serialize null objects");
    }
    out.write(0);
  }

  /**
   * Deserializes a <code>null</code> object.
   * Tries to read a single null-byte from the stream.
   *
   * @param in the stream to read from
   * @return <code>null</code>
   * @throws IOException if the stream can't be used
   * @throws SerializationException if the stream contains invalid data
   */
  @Override
  public Object deserialize(InputStream in) throws IOException, SerializationException {
    int i = in.read();
    if (i == -1) {
      throw new SerializationException("unexpected end of stream");
    } else if (i != 0) {
      throw new SerializationException("unexpected byte");
    }

    return null;
  }

  /**
   * Returns a {@link SerialBuilder} for <code>null</code> objects.
   *
   * @return new builder
   */
  @Override
  public SerialBuilder<Object> getBuilder() {
    return new NullSerialBuilder();
  }
}
