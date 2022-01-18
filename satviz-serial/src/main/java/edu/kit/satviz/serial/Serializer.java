package edu.kit.satviz.serial;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class Serializer<T> {

  public abstract void serialize(T t, OutputStream out);

  public abstract T deserialize(InputStream in);

  public abstract SerialBuilder<T> getBuilder();
}
