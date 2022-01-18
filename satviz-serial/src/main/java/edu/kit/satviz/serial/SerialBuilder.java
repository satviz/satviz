package edu.kit.satviz.serial;

public abstract class SerialBuilder<T> {

  public abstract boolean addByte(int i) throws SerializationException;

  public abstract T getObject();
}
