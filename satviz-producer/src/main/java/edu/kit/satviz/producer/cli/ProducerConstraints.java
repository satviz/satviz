package edu.kit.satviz.producer.cli;

import static edu.kit.satviz.common.Constraint.allOf;
import static edu.kit.satviz.common.Constraint.oneOf;
import static edu.kit.satviz.common.FileExistsConstraint.fileExists;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.producer.ProducerMode;
import java.util.List;
import java.util.Objects;

/**
 * A utility class containing a function to obtain an instance of {@code Constraint} that validates
 * producer parameter input.
 */
public final class ProducerConstraints {

  private ProducerConstraints() {

  }

  /**
   * Creates a constraint that ensures that a given instance of {@link ProducerParameters}
   * is valid.
   *
   * @param supportedModes The modes supported by this application.
   * @return A {@code Constraint<ProducerParameters>}.
   */
  public static Constraint<ProducerParameters> paramConstraints(
      List<? extends ProducerMode> supportedModes
  ) {
    Constraint<Object> isNull = Constraint.checking(Objects::isNull, "Is not null");
    return allOf(
        new SingleModeConstraint(supportedModes),
        oneOf(isNull, fileExists()).on(ProducerParameters::getInstanceFile),
        oneOf(isNull, fileExists()).on(ProducerParameters::getProofFile),
        oneOf(isNull, fileExists()).on(ProducerParameters::getSolverFile)
    );
  }

}
