package edu.kit.satviz.consumer.cli;

import edu.kit.satviz.common.PathArgumentType;
import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ConsumerMode;
import edu.kit.satviz.consumer.config.ConsumerModeConfig;
import edu.kit.satviz.consumer.config.EmbeddedModeConfig;
import edu.kit.satviz.consumer.config.EmbeddedModeSource;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import edu.kit.satviz.consumer.config.WeightFactor;
import java.util.Locale;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class ConsumerCli {

  /**
   * The {@code ArgumentParser}.
   */
  public static final ArgumentParser PARSER;

  static {
    PARSER = ArgumentParsers.newFor("sat-consumer").locale(Locale.ENGLISH).build()
        .defaultHelp(true)
        .version("0.1.0-SNAPSHOT")
        .description("""
            A clause consumer for satviz.
            Can be started with an external or embedded producer and a SAT instance.""");

    Subparsers subparsers = PARSER.addSubparsers();

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
        .help("Path to a DIMACS CNF instance file");
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
        .type(HeatmapColorsType.get())
        .help("Initial colors for the heatmap gradient");
    PARSER.addArgument("--start-rec")
        .type(boolean.class)
        .action(Arguments.storeTrue())
        .help("Start recording the animation immediately");
  }

  private ConsumerCli() {

  }

  public static ConsumerConfig parseArgs(String[] args) throws ArgumentParserException {
    ConsumerConfig config = new ConsumerConfig();
    ModeConfigParameters modeConfigParams = new ModeConfigParameters();
    PARSER.parseArgs(args, config);
    PARSER.parseArgs(args, modeConfigParams);

    // TODO: 25.02.2022 get ConsumerMode
    ConsumerMode mode = null;

    config.setModeConfig(getModeConfig(mode, modeConfigParams));
    return config;
  }

  private static ConsumerModeConfig getModeConfig(ConsumerMode mode, ModeConfigParameters params) {
    ConsumerModeConfig modeConfig;
    if (mode == ConsumerMode.EMBEDDED) {
      EmbeddedModeConfig embeddedConfig = new EmbeddedModeConfig();
      if (params.getProofFile() == null) {
        embeddedConfig.setSource(EmbeddedModeSource.SOLVER);
        embeddedConfig.setSourcePath(params.getSolverFile());
      } else {
        embeddedConfig.setSource(EmbeddedModeSource.PROOF);
        embeddedConfig.setSourcePath(params.getProofFile());
      }
      modeConfig = embeddedConfig;
    } else {
      ExternalModeConfig externalConfig = new ExternalModeConfig();
      externalConfig.setPort(params.getPort());
      modeConfig = externalConfig;
    }
    return modeConfig;
  }

}
