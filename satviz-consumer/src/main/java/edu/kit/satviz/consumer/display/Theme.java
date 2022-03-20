package edu.kit.satviz.consumer.display;

import edu.kit.satviz.consumer.bindings.NativeObject;
import edu.kit.satviz.consumer.config.HeatmapColors;
import java.lang.invoke.VarHandle;
import javafx.scene.paint.Color;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

public class Theme {

  public static final Color DEFAULT_BACKGROUND_COLOR = Color.color(0.3d, 0.3d, 0.3d);
  public static final Color DEFAULT_EDGE_COLOR = Color.color(1.0d, 1.0d, 1.0d);

  private static final MemoryLayout LAYOUT = NativeObject.paddedStruct(
      MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT).withName("bgColor"),
      MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT).withName("coldColor"),
      MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT).withName("hotColor"),
      MemoryLayout.sequenceLayout(3, CLinker.C_FLOAT).withName("edgeColor"),
      CLinker.C_FLOAT.withName("nodeSize")
  );

  private Color bgColor = DEFAULT_BACKGROUND_COLOR;
  private HeatmapColors heatmapColors = new HeatmapColors();
  private Color edgeColor = DEFAULT_EDGE_COLOR;
  private float nodeSize  = 10.0f;

  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
  }

  public void setHeatmapColors(HeatmapColors heatmapColors) {
    this.heatmapColors = heatmapColors;
  }

  public void setColdColor(Color coldColor) {
    this.heatmapColors.setColdColor(coldColor);
  }

  public void setHotColor(Color hotColor) {
    this.heatmapColors.setHotColor(hotColor);
  }

  public void setEdgeColor(Color edgeColor) {
    this.edgeColor = edgeColor;
  }

  public void setNodeSize(float size) {
    nodeSize = size;
  }

  public Color getBgColor() {
    return bgColor;
  }

  public Color getColdColor() {
    return heatmapColors.getColdColor();
  }

  public Color getHotColor() {
    return heatmapColors.getHotColor();
  }

  public Color getEdgeColor() {
    return edgeColor;
  }

  public float getNodeSize() {
    return nodeSize;
  }

  private void writeColor(MemorySegment segment, String name, float[] color) {
    for (int i = 0; i < 3; i++) {
      VarHandle handle = LAYOUT.varHandle(float.class,
          PathElement.groupElement(name),
          PathElement.sequenceElement(i));
      handle.set(segment, color[i]);
    }
  }

  private float[] convertColorToFloatArray(Color color) {
    return new float[] {
        (float) color.getRed(),
        (float) color.getGreen(),
        (float) color.getBlue()
    };
  }

  /**
   * Write the contents of this theme into a new native object.
   *
   * @param scope the resource scope to which the new native object should be bound.
   * @return a memory segment pointing to the native object.
   */
  public MemorySegment toSegment(ResourceScope scope) {
    MemorySegment segment = MemorySegment.allocateNative(LAYOUT, scope);

    writeColor(segment, "bgColor",   convertColorToFloatArray(bgColor));
    writeColor(segment, "coldColor", convertColorToFloatArray(heatmapColors.getColdColor()));
    writeColor(segment, "hotColor",  convertColorToFloatArray(heatmapColors.getHotColor()));
    writeColor(segment, "edgeColor", convertColorToFloatArray(edgeColor));

    LAYOUT.varHandle(float.class, PathElement.groupElement("nodeSize"))
        .set(segment, nodeSize);

    return segment;
  }

  @Override
  public boolean equals(Object o) {
    // TODO: 20.03.22
    return false;
  }

}
