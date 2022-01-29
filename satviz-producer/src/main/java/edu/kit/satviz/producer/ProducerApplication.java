package edu.kit.satviz.producer;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.network.ProducerConnection;
import edu.kit.satviz.network.ProducerId;
import edu.kit.satviz.producer.cli.ProducerCli;
import edu.kit.satviz.producer.cli.ProducerConstraints;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.mode.ProofMode;
import edu.kit.satviz.producer.mode.SolverMode;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProducerApplication {

  private static final Logger logger = Logger.getLogger("Producer");

  private static final List<ProducerMode> SUPPORTED_MODES
      = List.of(new ProofMode(), new SolverMode());

  public static void main(String[] args) {

    ProducerParameters parameters = parseArgs(args);
    validateArgs(parameters);
    ProducerMode selectedMode = SUPPORTED_MODES.stream()
        .filter(mode -> mode.isSet(parameters))
        .findFirst()
        .orElseThrow();

    try {
      ClauseSource source = selectedMode.createSource(parameters);
      ProducerConnection connection = new ProducerConnection(); // TODO: 29/01/2022 host, port
      connection.register(new ConnectionListener(connection, source));
      // TODO: 29/01/2022
      connection.establish(new ProducerId(null, null, null, false, 0));
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
