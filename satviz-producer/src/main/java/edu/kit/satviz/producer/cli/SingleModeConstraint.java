package edu.kit.satviz.producer.cli;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.producer.ProducerMode;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@code Constraint} implementation whose validation for a given instance of
 * {@link ProducerParameters} succeeds iff exactly 1 mode (from a given collection of modes) is set.
 */
public class SingleModeConstraint implements Constraint<ProducerParameters> {

  private final Collection<? extends ProducerMode> modes;

  /**
   * Creates a {@code SingleModeConstraint} using the given collection of modes.
   *
   * @param modes The {@link ProducerMode}s out of which exactly 1 must be set
   */
  public SingleModeConstraint(Collection<? extends ProducerMode> modes) {
    this.modes = modes;
  }

  @Override
  public void validate(ProducerParameters params) throws ConstraintValidationException {
    List<? extends ProducerMode> modesSet = modes.stream()
        .filter(mode -> mode.isSet(params))
        .toList();
    if (modesSet.isEmpty()) {
      fail("No producer mode set");
    } else if (modesSet.size() > 1) {
      fail(
          modesSet.stream().map(Object::toString).collect(Collectors.joining(", "))
          + " have been set as modes, but only one mode must be set at a time."
      );
    }
  }
}