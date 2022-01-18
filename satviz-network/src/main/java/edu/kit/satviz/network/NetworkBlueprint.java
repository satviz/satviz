package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.Serializer;
import java.io.OutputStream;
import java.util.Map;

/**
 * A mapping of message types to transmitted objects.
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
   * @param obj the object
   * @param type the object's type
   * @param out the stream to serialize to
   */
  public void serialize(Object obj, byte type, OutputStream out) {
    @SuppressWarnings("unchecked")
    Serializer<Object> s = (Serializer<Object>) typeMap.get(type);
    if (s != null) {
      s.serialize(obj, out);
    }
  }

  /**
   * Returns a deserialization builder according to the given type.
   * TODO should not return null, always a valid builder.
   *
   * @param type the type
   * @return a new builder for the given type of objects
   */
  public SerialBuilder<?> getBuilder(int type) {
    Serializer<?> s = typeMap.get((byte) type);
    if (s != null) {
      return s.getBuilder();
    }
    return null;
  }
}
