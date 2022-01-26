package edu.kit.satviz.consumer.config;

import java.util.Objects;

/**
 * This class stores two colors, which will then be used
 * to create the heatmap color spectrum.
 *
 * @author johnnyjayjay
 */
public class HeatmapColors {

  /**
   * The default "from"-color. (prone to change!)
   */
  public static final int DEFAULT_FROM_COLOR = 0xf4abff;

  /**
   * The default "to"-color. (prone to change too!)
   */
  public static final int DEFAULT_TO_COLOR = 0x882020;

  private int fromColor;
  private int toColor;

  /**
   * Simple constructor for instances of the <code>HeatmapColors</code> class.
   */
  public HeatmapColors() {
    this.fromColor = DEFAULT_FROM_COLOR;
    this.toColor = DEFAULT_TO_COLOR;
  }

  /**
   * This method sets the "from"-color.
   *
   * @param fromColor "from"-color.
   */
  public void setFromColor(int fromColor) {
    this.fromColor = fromColor;
  }

  /**
   * This method sets the "to"-color.
   *
   * @param toColor "to"-color.
   */
  public void setToColor(int toColor) {
    this.toColor = toColor;
  }

  /**
   * This simple getter-method returns the "from"-color.
   *
   * @return The "from"-color.
   */
  public int getFromColor() {
    return fromColor;
  }

  /**
   * This simple getter-method returns the "to"-color.
   *
   * @return The "to"-color.
   */
  public int getToColor() {
    return toColor;
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
    return fromColor == colors.fromColor && toColor == colors.toColor;
  }

  @Override
  public int hashCode() {
    return Objects.hash(fromColor, toColor);
  }
}
