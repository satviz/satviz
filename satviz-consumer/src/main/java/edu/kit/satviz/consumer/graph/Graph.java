package edu.kit.satviz.consumer.graph;

import edu.kit.satviz.consumer.bindings.NativeObject;
import java.io.InputStream;
import java.io.OutputStream;
import jdk.incubator.foreign.MemoryAddress;

public class Graph extends NativeObject {

  // Only protected because of mockup
  protected Graph(MemoryAddress pointer) {
    super(pointer);
  }

  public static Graph create(long nodes) {
    return null;
  }

  public void submitUpdate(GraphUpdate update) {
    update.submitTo(this);
  }

  public void recalculateLayout() {

  }

  public void adaptLayout() {

  }

  public void serialize(OutputStream stream) {

  }

  public void deserialize(InputStream stream) {

  }

  public NodeInfo queryNode(int index) {
    return null;
  }

  public EdgeInfo queryEdge(int index1, int index2) {
    return null;
  }

  public void destroy() {

  }

  @Override
  public void close() {
    destroy();
  }

}
