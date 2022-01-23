package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.Serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * A mapping of message types to transmitted objects.
 * This class maps bytes (encoding the message type) to {@link Serializer}s.
 *
 * @author luwae
 */
public class NetworkBlueprint {
  private final Map<Byte, Serializer<?>> typeMap;

  /**
   * Creates a new blueprint with specified type mapping.
   *
   * @param typeMap the mapping from type to serializer
   */
  public NetworkBlueprint(Map<Byte, Serializer<?>> typeMap) {
    this.typeMap = typeMap;
  }

  /**
   * Serializes an object according to its type.
   *
   * @param type the type
   * @param obj the object to serialize
   * @param out the stream to write to
   * @return whether the operation succeeded or not
   * @throws IOException if the stream cannot be used
   * @throws SerializationException if the serialization didn't work
   * @throws ClassCastException if the type is invalid or unknown
   */
  public void serialize(byte type, Object obj, OutputStream out) throws IOException,
      SerializationException, ClassCastException {
    Serializer<?> serial = typeMap.get(type);
    if (serial != null) {
      serial.serializeUnsafe(obj, out);
    }
    throw new ClassCastException("unknown type");
  }

  /**
   * Returns a deserialization builder according to the given type.
   *
   * @param type the type
   * @return a new builder for the given type of objects, <code>null</code> if not specified
   */
  public SerialBuilder<?> getBuilder(int type) {
    Serializer<?> s = typeMap.get((byte) type);
    if (s != null) {
      return s.getBuilder();
    }
    return null;
  }
}
