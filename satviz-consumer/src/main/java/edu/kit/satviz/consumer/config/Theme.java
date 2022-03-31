package edu.kit.satviz.consumer.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.kit.satviz.consumer.bindings.NativeObject;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import javafx.scene.paint.Color;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

/**
 * This class contains all parameters that are considered when first rendering the graph.
 */
public class Theme {

  /**
   * The default background color.
   */
  public static final Color DEFAULT_BACKGROUND_COLOR = Color.web("#CCCCCC");

  /**
   * The default edge color.
   */
  public static final Color DEFAULT_EDGE_COLOR = Color.web("#2E2A2C");

  /**
   * The default node size.
   */
  public static final float DEFAULT_NODE_SIZE = 10.0f;

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
  private float nodeSize = DEFAULT_NODE_SIZE;

  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
  }

  public void setHeatmapColors(HeatmapColors heatmapColors) {
    this.heatmapColors = heatmapColors;
  }

  /**
   * This method sets the cold color within the instance
   * of the {@code HeatmapColors} class.<br>
   * <p>
   * Same effect is achieved by entering {@code theme.getHeatmapColors().setColdColor(color);}
   * </p>
   *
   * @param coldColor The cold color.
   */
  public void setColdColor(Color coldColor) {
    this.heatmapColors.setColdColor(coldColor);
  }

  /**
   * This method sets the hot color within the instance
   * of the {@code HeatmapColors} class.<br>
   * <p>
   * Same effect is achieved by entering {@code theme.getHeatmapColors().setHotColor(color);}
   * </p>
   *
   * @param hotColor The hot color.
   */
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

  @JsonIgnore
  public HeatmapColors getHeatmapColors() {
    return heatmapColors;
  }

  /**
   * This getter-method returns the cold color within the
   * instance of the {@code HeatmapColors} class.<br>
   * <p>
   * Same effect is achieved by entering {@code theme.getHeatmapColors().getColdColor();}
   * </p>
   *
   * @return The cold color.
   */
  public Color getColdColor() {
    return heatmapColors.getColdColor();
  }

  /**
   * This getter-method returns the hot color within the
   * instance of the {@code HeatmapColors} class.<br>
   * <p>
   * Same effect is achieved by entering {@code theme.getHeatmapColors().getHotColor();}
   * </p>
   *
   * @return The hot color.
   */
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
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Theme theme = (Theme) o;
    return Objects.equals(bgColor, theme.bgColor)
        && Objects.equals(heatmapColors, theme.heatmapColors)
        && Objects.equals(edgeColor, theme.edgeColor)
        && nodeSize == theme.nodeSize;
  }

  @Override
  public int hashCode() {
    return Objects.hash(bgColor, heatmapColors, edgeColor, nodeSize);
  }

}
