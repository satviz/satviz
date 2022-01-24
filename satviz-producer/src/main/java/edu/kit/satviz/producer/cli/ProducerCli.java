package edu.kit.satviz.producer.cli;

import edu.kit.satviz.common.PathArgumentType;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

public final class ProducerCli {

  private static final ArgumentParser PARSER;

  static {
    PARSER = ArgumentParsers.newFor("sat-prod").build()
        .defaultHelp(true)
        .version("0.1.0-SNAPSHOT")
        .description("""
                        A clause producer for sat-viz.
                        Can be started with either a solver and a SAT instance or a proof.""");
    PARSER.addArgument("--instance", "-i")
        .type(PathArgumentType.get())
        .help("Path to a DIMACS CNF instance file or '-' for standard input");
    PARSER.addArgument("--solver", "-s")
        .type(PathArgumentType.get())
        .help("Path to an IPASIR solver shared library");
    PARSER.addArgument("--proof", "-p")
        .type(PathArgumentType.get())
        .help("Path to a DRAT proof or '-' for standard input");
    PARSER.addArgument("--host", "-H")
        .required(true)
        .type(String.class)
        .help("The host address of the target clause consumer");
    PARSER.addArgument("--port", "-P")
        .setDefault(34321)
        .type(int.class)
        .help("Port of the target clause consumer");
    PARSER.addArgument("--no-wait")
        .type(boolean.class)
        .help("Do not wait for a connection to be established, start solving immediately");
  }

  public static ProducerParameters parseArgs(String[] args) throws ArgumentParserException {
    ProducerParameters params = new ProducerParameters();
    PARSER.parseArgs(args, params);
    return params;
  }

}
