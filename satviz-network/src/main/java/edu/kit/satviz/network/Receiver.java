package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;

import java.nio.ByteBuffer;
import java.util.function.IntFunction;

public class Receiver {
  private final IntFunction<SerialBuilder<?>> gen;

  public Receiver(IntFunction<SerialBuilder<?>> gen) {
    this.gen = gen;
  }

  public boolean receive(ByteBuffer bb) {
    return false; // TODO
  }

  public NetworkObject getObject() {
    return null; // TODO
  }
}
