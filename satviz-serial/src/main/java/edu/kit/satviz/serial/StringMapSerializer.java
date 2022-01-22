package edu.kit.satviz.serial;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A {@link Serializer} for string key-value maps.
 * Uses <code>'='</code> to assign values, <code>'\n'</code> to separate key-value pairs,
 *     and <code>'\0'</code> to terminate the mapping.
 * Therefore, neither of these three characters may appear in any of the keys or values.
 * Uses UTF-8 to serialize strings.
 *
 * @author luwae
 */
public class StringMapSerializer extends Serializer<Map<String, String>> {

  @Override
  public void serialize(Map<String, String> map, OutputStream out) throws IOException, SerializationException {
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (key.contains("=") || key.contains("\n") || key.contains("\0")
          || value.contains("=") || value.contains("\n") || value.contains("\0")) {
        throw new SerializationException("encountered an invalid character");
      }
    }
    // do this separated in two loops to uphold the contract of serialize (see doc)
    int pairsToDo = map.size();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      out.write(key.getBytes(StandardCharsets.UTF_8));
      out.write('=');
      out.write(value.getBytes(StandardCharsets.UTF_8));
      out.write(--pairsToDo > 0 ? '\n' : '\0');
    }
  }

  @Override
  public SerialBuilder<Map<String, String>> getBuilder() {
    return new StringMapSerialBuilder();
  }
}
