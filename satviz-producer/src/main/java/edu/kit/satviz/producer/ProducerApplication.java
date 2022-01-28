package edu.kit.satviz.producer;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.producer.cli.ProducerCli;
import edu.kit.satviz.producer.cli.ProducerConstraints;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.mode.ProofMode;
import edu.kit.satviz.producer.mode.SolverMode;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

import java.util.List;

public class ProducerApplication {

  public static void main(String[] args) {
    List<ProducerMode> supportedModes = List.of(new ProofMode(), new SolverMode());
    Constraint<ProducerParameters> inputConstraint
        = ProducerConstraints.paramConstraints(supportedModes);

    ProducerParameters parameters;
    try {
      parameters = ProducerCli.parseArgs(args);
    } catch (ArgumentParserException e) {
      ProducerCli.PARSER.handleError(e);
      System.exit(1);
      return;
    }

    try {
      inputConstraint.validate(parameters);
    } catch (ConstraintValidationException e) {
      System.err.println(e.getMessage());
      System.exit(1);
      return;
    }

  }

}
