package edu.kit.satviz.network.general;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.Serializer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * A mapping of message types to transmitted objects.
 * This class maps bytes (encoding the message type) to {@link Serializer}s.
 */
public class NetworkBlueprint {
  private final Serializer<?>[] serializers = new Serializer<?>[256];

  /**
   * Creates a new blueprint with specified type mapping.
   *
   * @param typeMap the mapping from type to serializer
   */
  public NetworkBlueprint(Map<Byte, Serializer<?>> typeMap) {
    for (int i = 0; i < 256; i++) {
      serializers[i] = typeMap.get((byte) i);
    }
  }

  /**
   * Serializes an object according to its type.
   *
   * @param type the type
   * @param obj the object to serialize
   * @param out the stream to write to
   * @throws IOException if the stream cannot be used
   * @throws SerializationException if the serialization didn't work
   */
  public void serialize(byte type, Object obj, OutputStream out) throws IOException,
      SerializationException {
    Serializer<?> serial = serializers[Byte.toUnsignedInt(type)];
    if (serial != null) {
      serial.serializeUnsafe(obj, out);
    } else {
      throw new SerializationException("no serializer available");
    }
  }

  /**
   * Returns a deserialization builder according to the given type.
   *
   * @param type the type
   * @return a new builder for the given type of objects, <code>null</code> if not specified
   */
  public SerialBuilder<?> getBuilder(byte type) {
    Serializer<?> s = serializers[Byte.toUnsignedInt(type)];
    if (s != null) {
      return s.getBuilder();
    }
    return null;
  }
}
