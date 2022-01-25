package edu.kit.satviz.producer.cli;

import static edu.kit.satviz.common.FileExistsConstraint.fileExists;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.producer.ProducerMode;
import java.util.List;
import java.util.Objects;


public class ProducerConstraints {

  public static Constraint<ProducerParameters> paramConstraints(
      List<? extends ProducerMode> supportedModes
  ) {
    Constraint<Object> isNull = Constraint.checking(Objects::isNull, "Is not null");
    return Constraint.allOf(
        new SingleModeConstraint(supportedModes),
        fileExists().on(ProducerParameters::getInstanceFile),
        fileExists().on(ProducerParameters::getProofFile),
        fileExists().on(ProducerParameters::getSolverFile)
    );
  }


  public static void validate(ProducerParameters parameters) throws ConstraintValidationException {

  }

}
