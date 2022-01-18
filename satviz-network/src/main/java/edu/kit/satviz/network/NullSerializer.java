package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NullSerializer extends Serializer<Object> {
  private boolean done = false;

  @Override
  public void serialize(Object o, OutputStream out) throws IOException, SerializationException {
    out.write(0);
  }

  @Override
  public Object deserialize(InputStream in) throws IOException, SerializationException {
    if (done) {
      throw new SerializationException("done");
    }

    int i = in.read();
    if (i == -1) {
      throw new SerializationException("unexpected end of stream");
    } else if (i != 0) {
      throw new SerializationException("unexpected byte");
    }

    return null;
  }

  @Override
  public SerialBuilder<Object> getBuilder() {
    return null;
  }
}
