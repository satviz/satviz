package edu.kit.satviz.serial;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * A {@link SerialBuilder} for strings.
 * Deserializes strings according to UTF-8.
 *
 * @author luwae
 */
public class StringSerialBuilder extends SerialBuilder<String> {
  private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
  private String finishedString;

  @Override
  public boolean addByte(byte b) throws SerializationException {
    if (objectFinished()) {
      throw new SerializationException("done");
    }

    bytes.write(b);
    if (b == (byte) '\0') {
      finishedString = bytes.toString(StandardCharsets.UTF_8);
      return true;
    }
    return false;
  }

  @Override
  public boolean objectFinished() {
    return finishedString != null;
  }

  @Override
  public String getObject() {
    return finishedString;
  }

  @Override
  public void reset() {
    bytes.reset();
    finishedString = null;
  }
}
