package edu.kit.satviz.consumer.cli;

import static edu.kit.satviz.common.Constraint.allOf;
import static edu.kit.satviz.common.FileExistsConstraint.fileExists;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.consumer.config.ConsumerConfig;
import java.nio.file.Paths;

public final class ConsumerConstraints {

  private ConsumerConstraints() {

  }

  public static Constraint<ConsumerConfig> paramConstraints() {
    return allOf(fileExists().on(ConsumerConfig::getInstancePath),
        fileExists().on(e -> Paths.get(e.getVideoTemplatePath()).getParent()),
        new ConsumerModeConstraint());
  }

}
