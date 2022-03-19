package edu.kit.satviz.consumer.cli;

import static edu.kit.satviz.common.FileExistsConstraint.fileExists;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ConsumerMode;
import edu.kit.satviz.consumer.config.ConsumerModeConfig;
import edu.kit.satviz.consumer.config.EmbeddedModeConfig;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class defines the constraints for a valid {@code ConsumerConfig} instance.
 */
public class ConsumerConstraint implements Constraint<ConsumerConfig> {

  @Override
  public void validate(ConsumerConfig config) throws ConstraintValidationException {
    if (config.getInstancePath() == null) {
      fail("No instance is set");
    }
    fileExists().validate(config.getInstancePath());
    if (config.getVideoTemplatePath() == null) {
      fail("No video template path ist set");
    }
    fileExists().validate(Paths.get(config.getVideoTemplatePath()).getParent());
    ConsumerModeConfig modeConfig = config.getModeConfig();
    if (modeConfig.getMode() == ConsumerMode.EMBEDDED) {
      EmbeddedModeConfig embeddedConfig = (EmbeddedModeConfig) modeConfig;
      if (embeddedConfig.getSourcePath() == null) {
        fail("No source path is set");
      } else if (Files.isDirectory(embeddedConfig.getSourcePath())) {
        fail("Directory is set as source");
      }
      fileExists().validate(embeddedConfig.getSourcePath());
    } else if (modeConfig.getMode() == ConsumerMode.EXTERNAL) {
      ExternalModeConfig externalConfig = (ExternalModeConfig) modeConfig;
      int port = externalConfig.getPort();
      if (port < ExternalModeConfig.MIN_PORT_NUMBER || port > ExternalModeConfig.MAX_PORT_NUMBER) {
        fail("Invalid port set");
      }
    } else {
      fail("No consumer mode set");
    }
  }

}
