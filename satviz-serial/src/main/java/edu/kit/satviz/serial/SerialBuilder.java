package edu.kit.satviz.serial;

import java.nio.ByteBuffer;

public abstract class SerialBuilder<T> {

  public abstract boolean addByte(int i) throws SerializationException;

  public boolean addBytes(ByteBuffer bb) throws SerializationException {
    // naive; @override for more efficient implementations
    while (bb.hasRemaining()) {
      if (addByte(bb.get())) {
        return true;
      }
    }
    return false;
  }

  public abstract boolean objectFinished();

  public abstract T getObject();
}
