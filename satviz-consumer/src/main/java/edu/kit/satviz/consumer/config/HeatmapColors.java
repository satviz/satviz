package edu.kit.satviz.consumer.config;

public class HeatmapColors {

  /**
   * The default "from"-color. (prone to change!)
   */
  public static final int DEFAULT_FROM_COLOR = 0xf4abff;
  /**
   * The default "to"-color. (prone to change too!)
   */
  public static final int DEFAULT_TO_COLOR = 0x882020;

  private int fromColor = DEFAULT_FROM_COLOR;
  private int toColor = DEFAULT_TO_COLOR;

  public int getFromColor() {
    return fromColor;
  }

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
    // TODO
    return super.hashCode();
  }

}
