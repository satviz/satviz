package edu.kit.satviz.serial;

import java.io.ByteArrayOutputStream;

public class StringSerialBuilder extends SerialBuilder<String> {
  private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
  private String finishedString;

  @Override
  public boolean addByte(byte b) throws SerializationException {
    if (finishedString != null) {
      throw new SerializationException("done");
    }

    bytes.write(b);
    if (b == 0) {
      finishedString = bytes.toString();
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
}
