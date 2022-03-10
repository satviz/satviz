package edu.kit.satviz.consumer.display;

import edu.kit.satviz.consumer.bindings.Struct;
import jdk.incubator.foreign.*;

public class Theme {
  private static final Struct STRUCT = Struct.builder()
      .field("coldColor", float.class, MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT))
      .field("hotColor",  float.class, MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT))
      .field("edgeColor", float.class, MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT))
      .field("nodeSize",  float.class, CLinker.C_FLOAT)
      .build();

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
    MemorySegment segment = STRUCT.allocateNew(scope);

    for (int i = 0; i < 3; i++) {
      STRUCT.varHandle("coldColor").set(segment, i, coldColor[i]);
    }
    for (int i = 0; i < 3; i++) {
      STRUCT.varHandle("hotColor").set(segment, i, hotColor[i]);
    }
    for (int i = 0; i < 3; i++) {
      STRUCT.varHandle("edgeColor").set(segment, i, edgeColor[i]);
    }
    STRUCT.varHandle("nodeSize").set(segment, nodeSize);

    return segment;
  }
}
