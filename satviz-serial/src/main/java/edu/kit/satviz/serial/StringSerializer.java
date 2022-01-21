package edu.kit.satviz.serial;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * A {@link Serializer} for strings.
 * Serializes strings according to UTF-8.
 *
 * @author luwae
 */
public class StringSerializer extends Serializer<String> {

  @Override
  public void serialize(String s, OutputStream out) throws IOException {
    out.write(s.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public SerialBuilder<String> getBuilder() {
    return new StringSerialBuilder();
  }
}
