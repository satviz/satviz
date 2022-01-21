package edu.kit.satviz.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IntSerializer extends Serializer<Integer> {
  @Override
  public void serialize(Integer i, OutputStream out) throws IOException, SerializationException {
    int _i = i;
    out.write((byte) _i);
    out.write((byte) (_i >> 8));
    out.write((byte) (_i >> 16));
    out.write((byte) (_i >> 24));
  }

  @Override
  public Integer deserialize(InputStream in) throws IOException, SerializationException {
    int i = 0;
    for (int numByte = 0; numByte < 4; numByte++) {
      int readByte = in.read();
      if (readByte == -1) {
        throw new IllegalArgumentException("unexpected end of stream");
      }
      i |= readByte << (numByte << 3);
    }
    return i;
  }

  @Override
  public SerialBuilder<Integer> getBuilder() {
    return new IntSerialBuilder();
  }
}
