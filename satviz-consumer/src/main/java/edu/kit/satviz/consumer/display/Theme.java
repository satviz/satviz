package edu.kit.satviz.consumer.display;

import edu.kit.satviz.consumer.bindings.NativeObject;
import jdk.incubator.foreign.*;
import jdk.incubator.foreign.MemoryLayout.PathElement;

import java.lang.invoke.VarHandle;

public class Theme {
  private static final MemoryLayout LAYOUT = NativeObject.paddedStruct(
      MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT).withName("bgColor"),
      MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT).withName("coldColor"),
      MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT).withName("hotColor"),
      MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT).withName("edgeColor"),
      CLinker.C_FLOAT.withName("nodeSize")
  );

  private float[] bgColor   = { 0.3f, 0.3f, 0.3f };
  private float[] coldColor = { 0.0f, 0.0f, 1.0f };
  private float[] hotColor  = { 1.0f, 0.0f, 0.0f };
  private float[] edgeColor = { 1.0f, 1.0f, 1.0f };
  private float   nodeSize  = 10.0f;

  public void setBgColor  (float r, float g, float b) { bgColor   = new float[]{r, g, b}; }
  public void setColdColor(float r, float g, float b) { coldColor = new float[]{r, g, b}; }
  public void setHotColor (float r, float g, float b) { hotColor  = new float[]{r, g, b}; }
  public void setEdgeColor(float r, float g, float b) { edgeColor = new float[]{r, g, b}; }
  public void setNodeSize (float size) { nodeSize = size; }

  public float[] getBgColor  () { return bgColor; }
  public float[] getColdColor() { return coldColor; }
  public float[] getHotColor () { return hotColor; }
  public float[] getEdgeColor() { return edgeColor; }
  public float   getNodeSize () { return nodeSize; }

  private void writeColor(MemorySegment segment, String name, float[] color) {
    for (int i = 0; i < 3; i++) {
      VarHandle handle = LAYOUT.varHandle(float.class,
          PathElement.groupElement(name),
          PathElement.sequenceElement(i));
      handle.set(segment, color[i]);
    }
  }

  public MemorySegment toSegment(ResourceScope scope) {
    MemorySegment segment = MemorySegment.allocateNative(LAYOUT, scope);

    writeColor(segment, "bgColor",   bgColor);
    writeColor(segment, "coldColor", coldColor);
    writeColor(segment, "hotColor",  hotColor);
    writeColor(segment, "edgeColor", edgeColor);

    LAYOUT.varHandle(float.class, PathElement.groupElement("nodeSize"))
        .set(segment, nodeSize);

    return segment;
  }
}
