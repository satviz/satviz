package edu.kit.satviz.consumer.cli;

import edu.kit.satviz.consumer.config.HeatmapColors;
import edu.kit.satviz.consumer.gui.GuiUtils;
import javafx.scene.paint.Color;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;

/**
 * An {@code ArgumentType} implementation that parses its input to a
 * {@link edu.kit.satviz.consumer.config.HeatmapColors} object.<br>
 * This class is a singleton.
 *
 * @see #get()
 */
public final class HeatmapColorsType implements ArgumentType<HeatmapColors> {

  private static final HeatmapColorsType INSTANCE = new HeatmapColorsType();

  private HeatmapColorsType() {

  }

  @Override
  public HeatmapColors convert(ArgumentParser parser, Argument arg, String value)
      throws ArgumentParserException {
    if (!value.matches("#([a-fA-F0-9]{6}):#([a-fA-F0-9]{6})")) {
      throw new ArgumentParserException("Invalid heatmap colors.", parser);
    }
    String[] stringArray = value.split(":");
    HeatmapColors heatmapColors = new HeatmapColors();
    heatmapColors.setHotColor(intToColor(Integer.parseInt(stringArray[0].substring(1))));
    heatmapColors.setColdColor(intToColor(Integer.parseInt(stringArray[1].substring(1))));
    return heatmapColors;
  }

  /**
   * Parses an integer representation of a color into the corresponding {@link Color} object.
   *
   * @param color The color to be parsed.
   * @return The parsed color.
   */
  private static Color intToColor(int color) {
    int red = (color >>> 16) & 0xFF;
    int green = (color >>> 8) & 0xFF;
    int blue = color & 0xFF;
    return new Color(red / 255.0, green / 255.0, blue / 255.0, 1.0);
  }

  /**
   * Gets the singleton instance of this ArgumentType.
   *
   * @return The {@code HeatmapColorsType} instance.
   */
  public static HeatmapColorsType get() {
    return INSTANCE;
  }

}
