package edu.kit.satviz.consumer.config;

import java.util.Objects;
import javafx.scene.paint.Color;

/**
 * This class stores two colors, which will then be used
 * to create the heatmap color spectrum.
 */
public class HeatmapColors {

  /**
   * The default hot color.
   */
  public static final Color DEFAULT_HOT_COLOR = Color.color(1, 0, 0);

  /**
   * The default cold color.
   */
  public static final Color DEFAULT_COLD_COLOR = Color.color(0, 0, 1);

  private Color hotColor;
  private Color coldColor;

  /**
   * Simple constructor for instances of the <code>HeatmapColors</code> class.
   */
  public HeatmapColors() {
    this.hotColor = DEFAULT_HOT_COLOR;
    this.coldColor = DEFAULT_COLD_COLOR;
  }

  /**
   * This method sets the hot color.
   *
   * @param hotColor The hot color.
   */
  public void setHotColor(Color hotColor) {
    this.hotColor = hotColor;
  }

  /**
   * This method sets the cold color.
   *
   * @param coldColor The cold color.
   */
  public void setColdColor(Color coldColor) {
    this.coldColor = coldColor;
  }

  /**
   * This simple getter-method returns the hot color.
   *
   * @return The hot color.
   */
  public Color getHotColor() {
    return hotColor;
  }

  /**
   * This simple getter-method returns the cold color.
   *
   * @return The cold color.
   */
  public Color getColdColor() {
    return coldColor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HeatmapColors colors = (HeatmapColors) o;
    return hotColor.equals(colors.hotColor) && coldColor.equals(colors.coldColor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hotColor, coldColor);
  }
}
