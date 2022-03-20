package edu.kit.satviz.consumer.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.satviz.common.PathArgumentType;
import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ConsumerModeConfig;
import edu.kit.satviz.consumer.config.EmbeddedModeConfig;
import edu.kit.satviz.consumer.config.EmbeddedModeSource;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import edu.kit.satviz.consumer.config.HeatmapColors;
import edu.kit.satviz.consumer.config.WeightFactor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

import edu.kit.satviz.consumer.display.Theme;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * A utility class defining the command line {@code ArgumentParser} used by the consumer.
 */
public final class ConsumerCli {

  /**
   * The {@code ArgumentParser}.
   */
  public static final ArgumentParser PARSER;

  static {
    PARSER = ArgumentParsers.newFor("satviz").locale(Locale.ENGLISH).build()
        .defaultHelp(true)
        .version("0.1.0-SNAPSHOT")
        .description("""
            A clause consumer for satviz.
            Can be started with an external or embedded producer and a SAT instance.""");

    Subparsers subparsers = PARSER.addSubparsers().dest("subparser_name");

    Subparser embeddedParser = subparsers.addParser("embedded");
    embeddedParser.addArgument("--solver", "-s")
        .type(PathArgumentType.get())
        .help("Path to an IPASIR solver shared library");
    embeddedParser.addArgument("--proof", "-p")
        .type(PathArgumentType.get())
        .help("Path to a DRAT proof");

    Subparser externalParser = subparsers.addParser("external");
    externalParser.addArgument("--port", "-P")
        .setDefault(ExternalModeConfig.DEFAULT_PORT_NUMBER)
        .type(int.class)
        .help("Port, where clauses can be received");

    PARSER.addArgument("--instance", "-i")
        .type(PathArgumentType.get())
        .help("Path to a DIMACS CNF instance file")
        .required(true);
    PARSER.addArgument("--file", "-f")
        .type(PathArgumentType.get())
        .help("Path to configuration file");
    PARSER.addArgument("--no-gui")
        .type(boolean.class)
        .action(Arguments.storeTrue())
        .help("Run the application without the graphical components");
    PARSER.addArgument("--out", "-o")
        .setDefault(ConsumerConfig.DEFAULT_VIDEO_TEMPLATE_PATH)
        .type(String.class)
        .help("Path, where animation should be stored to, in this format: /etc/video-{}.ogv");
    PARSER.addArgument("--buffer", "-b")
        .setDefault(ConsumerConfig.DEFAULT_BUFFER_SIZE)
        .type(int.class)
        .help("Initial buffer size");
    PARSER.addArgument("--weight", "-w")
        .setDefault(ConsumerConfig.DEFAULT_WEIGHT_FACTOR)
        .type(WeightFactor.class)
        .help("Initial weight factor");
    PARSER.addArgument("--window", "-W")
        .setDefault(ConsumerConfig.DEFAULT_WINDOW_SIZE)
        .type(int.class)
        .help("Initial heatmap window size");
    PARSER.addArgument("--colors", "-c")
        .setDefault(new HeatmapColors())
        .type(HeatmapColorsType.get())
        .help("Initial colors for the heatmap gradient");
    PARSER.addArgument("--start-rec")
        .type(boolean.class)
        .action(Arguments.storeTrue())
        .help("Start recording the animation immediately");
  }

  private ConsumerCli() {

  }

  /**
   * Parses some arguments given as a string array to an instance of {@link ConsumerConfig}
   * using the {@code ArgumentParser} defined by this class.
   *
   * @param args The command line args.
   * @return The parsed arguments as a {@link ConsumerConfig} instance.
   * @throws ArgumentParserException if the underlying {@code ArgumentParser} throws.
   */
  public static ConsumerConfig parseArgs(String[] args)
      throws ArgumentParserException {
    Namespace namespace = PARSER.parseArgs(args);
    ConsumerConfig config;
    if (namespace.get("file") != null) {
      config = parseConfigFile(((Path) namespace.get("file")).toFile());
    } else {
      config = getConsumerConfigFromArgs(namespace);
    }
    return config;
  }

  private static ConsumerConfig parseConfigFile(File configFile)
      throws ArgumentParserException {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(configFile, ConsumerConfig.class);
    } catch (IOException e) {
      throw new ArgumentParserException("Invalid configuration file.", PARSER);
    }
  }

  private static ConsumerConfig getConsumerConfigFromArgs(Namespace namespace) {
    ConsumerConfig config = new ConsumerConfig();
    config.setModeConfig(getConsumerModeConfig(namespace));
    config.setInstancePath(namespace.get("instance"));
    config.setNoGui(namespace.getBoolean("no_gui"));
    config.setVideoTemplatePath(namespace.getString("out"));
    config.setRecordImmediately(namespace.getBoolean("start_rec"));
    config.setBufferSize(namespace.getInt("buffer"));
    config.setWeightFactor(namespace.get("weight"));
    config.setWindowSize(namespace.getInt("window"));
    Theme theme = new Theme();
    HeatmapColors heatmapColors = namespace.get("colors");
    theme.setHeatmapColors(heatmapColors);
    config.setTheme(theme);
    return config;
  }

  private static ConsumerModeConfig getConsumerModeConfig(Namespace namespace) {
    return switch (namespace.getString("subparser_name")) {
      case "embedded" -> {
        EmbeddedModeConfig embeddedConfig = new EmbeddedModeConfig();
        if (namespace.get("solver") != null) {
          embeddedConfig.setSource(EmbeddedModeSource.SOLVER);
          embeddedConfig.setSourcePath(namespace.get("solver"));
        } else {
          embeddedConfig.setSource(EmbeddedModeSource.PROOF);
          embeddedConfig.setSourcePath(namespace.get("proof"));
        }
        yield embeddedConfig;
      }
      case "external" -> {
        ExternalModeConfig externalConfig = new ExternalModeConfig();
        externalConfig.setPort(namespace.get("port"));
        yield externalConfig;
      }
      default -> throw new IllegalArgumentException("No valid mode set.");
    };
  }

}
