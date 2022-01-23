package edu.kit.satviz.producer.cli;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.producer.ProducerMode;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SingleModeConstraint implements Constraint<ProducerParameters> {

  private final Collection<ProducerMode> modes;

  public SingleModeConstraint(Collection<ProducerMode> modes) {
    this.modes = modes;
  }

  @Override
  public void validate(ProducerParameters params) throws ConstraintValidationException {
    List<ProducerMode> modesSet = modes.stream().filter(mode -> mode.isSet(params)).toList();
    if (modesSet.size() == 0) {
      fail("No producer mode set");
    } else if (modesSet.size() > 1) {
      fail(
          modesSet.stream().map(Object::toString).collect(Collectors.joining(", "))
          + " have been set as modes, but only one mode must be set at a time."
      );
    }
  }
}