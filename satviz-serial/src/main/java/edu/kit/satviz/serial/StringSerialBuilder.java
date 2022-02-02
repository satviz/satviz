package edu.kit.satviz.serial;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * A {@link SerialBuilder} for strings.
 * Uses UTF-8 to deserialize strings.
 */
public class StringSerialBuilder extends SerialBuilder<String> {
  private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
  private String finishedString;

  @Override
  protected void processAddByte(byte b) {
    if (b != (byte) '\0') {
      bytes.write(b);
    } else {
      finishedString = bytes.toString(StandardCharsets.UTF_8);
      finish();
    }
  }

  @Override
  protected String processGetObject() {
    return finishedString;
  }

  @Override
  protected void processReset() {
    bytes.reset();
    finishedString = null;
  }
}
