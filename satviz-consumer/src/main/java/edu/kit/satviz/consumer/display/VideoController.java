package edu.kit.satviz.consumer.display;

import edu.kit.satviz.consumer.bindings.NativeObject;
import jdk.incubator.foreign.MemoryAddress;

public class VideoController extends NativeObject {

  private VideoController(MemoryAddress pointer) {
    super(pointer);
  }


  @Override
  public void close() {

  }
}
