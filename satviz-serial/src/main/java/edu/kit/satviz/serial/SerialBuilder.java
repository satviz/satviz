package edu.kit.satviz.serial;

public abstract class SerialBuilder<T> {

  public abstract boolean addByte(int i);

  public abstract T getObject();
}
