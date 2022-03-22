package edu.kit.satviz.producer;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.network.pub.ProducerConnection;
import edu.kit.satviz.producer.cli.ProducerCli;
import edu.kit.satviz.producer.cli.ProducerConstraints;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.mode.ProofMode;
import edu.kit.satviz.producer.mode.SolverMode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

/**
 * The main class of the producer application.
 */
public class ProducerApplication {

  private static final Logger logger = Logger.getLogger("Producer");

  // new modes can be added to this list
  private static final List<ProducerMode> SUPPORTED_MODES
      = List.of(new ProofMode(), new SolverMode());

  /**
   * The entry point of the application.
   *
   * @param args The command line args, as defined by the CLI specification.
   */
  public static void main(String[] args) {
    ProducerParameters parameters = parseArgs(args);
    validateArgs(parameters);
    ProducerMode selectedMode = SUPPORTED_MODES.stream()
        .filter(mode -> mode.isSet(parameters))
        .findFirst()
        .orElseThrow();

    try {
      logger.info("Opening clause source");
      ProducerModeData data = selectedMode.apply(parameters);
      logger.info("Clause source opened");
      ProducerConnection connection = new ProducerConnection(
          parameters.getHost(), parameters.getPort());
      SourceControlConnectionListener ls = new SourceControlConnectionListener(connection,
          data.source());
      logger.info("Waiting for network connection...");
      connection.establish(data.id(), ls);
    } catch (SourceException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      System.exit(1);
    }

  }

  private static ProducerParameters parseArgs(String[] args) {
    try {
      return ProducerCli.parseArgs(args);
    } catch (ArgumentParserException e) {
      ProducerCli.PARSER.handleError(e);
      System.exit(1);
      return null;
    }
  }

  private static void validateArgs(ProducerParameters params) {
    Constraint<ProducerParameters> inputConstraint
        = ProducerConstraints.paramConstraints(SUPPORTED_MODES);
    try {
      inputConstraint.validate(params);
    } catch (ConstraintValidationException e) {
      logger.severe(e.getMessage());
      System.exit(1);
    }
  }


}
