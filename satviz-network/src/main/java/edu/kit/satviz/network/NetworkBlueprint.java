package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.Serializer;
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
   * Returns the serializer corresponding to a type.
   *
   * @param type the type
   * @return the serializer matching the type, <code>null</code> if not specified
   */
  public Serializer<?> getSerializer(byte type) {
    return typeMap.get(type);
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
