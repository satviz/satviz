package edu.kit.satviz.consumer.config;

import static edu.kit.satviz.common.FileExistsConstraint.fileExists;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class defines the constraints for a valid {@code ConsumerConfig} instance.
 */
public class ConsumerConstraint implements Constraint<ConsumerConfig> {

  @Override
  public void validate(ConsumerConfig config) throws ConstraintValidationException {
    if (config.getVideoTimeout() < 0) {
      fail("Timeout must be positive");
    }

    if (config.getInstancePath() == null) {
      fail("No instance is set");
    }
    fileExists().validate(config.getInstancePath());
    fileExists().validate(Paths.get(config.getVideoTemplatePath()).toAbsolutePath().getParent());
    ConsumerModeConfig modeConfig = config.getModeConfig();
    if (modeConfig.getMode() == ConsumerMode.EMBEDDED) {
      EmbeddedModeConfig embeddedConfig = (EmbeddedModeConfig) modeConfig;
      Path source = embeddedConfig.getSourcePath();
      if (source == null) {
        fail("No source path is set");
      } else if (!Files.isRegularFile(source)) {
        fail("Source is not a regular file");
      } else if (!Files.isReadable(source)) {
        fail("Source is not readable");
      }
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
