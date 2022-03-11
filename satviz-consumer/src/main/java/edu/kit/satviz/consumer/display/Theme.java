package edu.kit.satviz.consumer.display;

import edu.kit.satviz.consumer.bindings.NativeObject;
import jdk.incubator.foreign.*;
import jdk.incubator.foreign.MemoryLayout.PathElement;

import java.lang.invoke.VarHandle;

public class Theme {
  private float[] coldColor = { 0.0f, 0.0f, 1.0f };
  private float[] hotColor  = { 1.0f, 0.0f, 0.0f };
  private float[] edgeColor = { 1.0f, 1.0f, 1.0f };
  private float   nodeSize  = 10.0f;

  public void setColdColor(float[] color) { coldColor = color; }
  public void setHotColor (float[] color) { hotColor  = color; }
  public void setEdgeColor(float[] color) { edgeColor = color; }
  public void setNodeSize (float size) { nodeSize = size; }

  public float[] getColdColor() { return coldColor; }
  public float[] getHotColor () { return hotColor; }
  public float[] getEdgeColor() { return edgeColor; }
  public float   getNodeSize () { return nodeSize; }

  public MemorySegment toSegment(ResourceScope scope) {
    MemoryLayout layout = NativeObject.paddedStruct(
        MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT).withName("coldColor"),
        MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT).withName("hotColor"),
        MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT).withName("edgeColor"),
        CLinker.C_FLOAT.withName("nodeSize")
    );

    MemorySegment segment = MemorySegment.allocateNative(layout, scope);

    for (int i = 0; i < 3; i++) {
      VarHandle handle = layout.varHandle(float.class,
          PathElement.groupElement("coldColor"),
          PathElement.sequenceElement(i));
      handle.set(segment, coldColor[i]);
    }

    for (int i = 0; i < 3; i++) {
      VarHandle handle = layout.varHandle(float.class,
          PathElement.groupElement("hotColor"),
          PathElement.sequenceElement(i));
      handle.set(segment, hotColor[i]);
    }

    for (int i = 0; i < 3; i++) {
      VarHandle handle = layout.varHandle(float.class,
          PathElement.groupElement("edgeColor"),
          PathElement.sequenceElement(i));
      handle.set(segment, edgeColor[i]);
    }

    layout.varHandle(float.class, PathElement.groupElement("nodeSize"))
        .set(segment, nodeSize);

    return segment;
  }
}
