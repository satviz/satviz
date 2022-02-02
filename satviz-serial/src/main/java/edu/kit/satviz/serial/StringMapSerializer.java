package edu.kit.satviz.serial;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A {@link Serializer} for string key-value maps.
 * Uses UTF-8 to serialize strings.
 */
public class StringMapSerializer extends Serializer<Map<String, String>> {
  private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

  @Override
  public void serialize(Map<String, String> map, OutputStream out) throws IOException,
      SerializationException {
    // do this in two loops to avoid out being written if something goes wrong
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (key.contains("\0") || value.contains("\0")) {
        throw new SerializationException("encountered an invalid character");
      }
    }

    int pairsToDo = map.size();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      writeEscape(key, out);
      out.write('=');
      writeEscape(value, out);
      out.write(--pairsToDo > 0 ? '\n' : '\0');
    }
  }

  private void writeEscape(String s, OutputStream out) throws IOException {
    bytes.reset();
    for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
      if (b == (byte) '=' || b == (byte) '\n' || b == (byte) '\\') {
        bytes.write('\\');
      }
      bytes.write(b);
    }
    out.write(bytes.toByteArray());
  }

  @Override
  public SerialBuilder<Map<String, String>> getBuilder() {
    return new StringMapSerialBuilder();
  }
}
