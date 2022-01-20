package edu.kit.satviz.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Serializer<T> {

  public abstract void serialize(T t, OutputStream out) throws IOException, SerializationException;

  public void serializeUnsafe(Object o, OutputStream out) throws IOException,
      ClassCastException, SerializationException {
    @SuppressWarnings("unchecked")
    T t = (T) o;
    serialize(t, out);
  }

  public abstract T deserialize(InputStream in) throws IOException, SerializationException;

  public abstract SerialBuilder<T> getBuilder();
}
