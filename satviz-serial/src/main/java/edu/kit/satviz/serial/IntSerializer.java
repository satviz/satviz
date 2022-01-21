package edu.kit.satviz.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link Serializer} for integers.
 *
 * @author luwae
 */
public class IntSerializer extends Serializer<Integer> {
  @Override
  public void serialize(Integer i, OutputStream out) throws IOException {
    int primitive = i;
    out.write((byte) primitive);
    out.write((byte) (primitive >> 8));
    out.write((byte) (primitive >> 16));
    out.write((byte) (primitive >> 24));
  }

  @Override
  public Integer deserialize(InputStream in) throws IOException {
    // overwritten to avoid an abundance of builders to be created
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
