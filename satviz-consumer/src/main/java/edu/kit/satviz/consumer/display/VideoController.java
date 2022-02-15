package edu.kit.satviz.consumer.display;

import edu.kit.satviz.consumer.bindings.NativeObject;
import edu.kit.satviz.consumer.graph.Graph;
import jdk.incubator.foreign.MemoryAddress;

public class VideoController extends NativeObject {


  private VideoController(MemoryAddress pointer) {
    super(pointer);
  }

  public static VideoController create(Graph graph, DisplayType displayType) {
    return null;
  }

  public boolean startRecording(String fileName, String encoder) {
    return false;
  }

  public void stopRecording() {

  }

  public void resumeRecording() {

  }

  public void finishRecording() {

  }

  public void destroy() {

  }

  @Override
  public void close() {
  }
}
