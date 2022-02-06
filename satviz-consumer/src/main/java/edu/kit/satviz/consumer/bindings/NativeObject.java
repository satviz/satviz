package edu.kit.satviz.consumer.bindings;

import jdk.incubator.foreign.MemoryAddress;

public abstract class NativeObject implements AutoCloseable {

  private final MemoryAddress pointer;

  protected NativeObject(MemoryAddress pointer) {
    this.pointer = pointer;
  }

  public MemoryAddress getPointer() {
    return pointer;
  }

  @Override
  public abstract void close();
}
