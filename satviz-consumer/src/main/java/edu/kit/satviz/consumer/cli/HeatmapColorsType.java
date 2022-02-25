package edu.kit.satviz.consumer.cli;

import edu.kit.satviz.consumer.config.HeatmapColors;
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
  public HeatmapColors convert(ArgumentParser parser, Argument arg, String value) throws ArgumentParserException {
    HeatmapColors heatmapColors = new HeatmapColors();
    // TODO: 25.02.2022
    return heatmapColors;
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
