package edu.kit.satviz.consumer.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsumerCliTest {

  private ConsumerConfig validConfig1;
  private String[] validArguments1;
  private String[] fileArgument;
  private String[] invalidArguments1;
  private String[] invalidArguments2;

  @BeforeEach
  void setUp() throws IOException {
    Path tempInstance = Files.createTempFile("instance",".cnf");
    Path tempVideoTemplate = Files.createTempFile("video", ".ogv");
    Path configPath = Files.createTempFile("config", ".json");
    Files.copy(
        ConsumerCliTest.class.getResourceAsStream("/config2.json"),
        configPath,
        StandardCopyOption.REPLACE_EXISTING
    );

    validArguments1 = new String[] {
        "-i", tempInstance.toString(), "-o", tempVideoTemplate.toString(), "external", "-P", "1231"
    };
    fileArgument = new String[] {"config", configPath.toString()};
    invalidArguments1 = new String[] {"external", "-P", "1231"};
    invalidArguments2 = new String[] {"embedded"}; // should not throw NullPointerException

    validConfig1 = new ConsumerConfig();
    ExternalModeConfig externalModeConfig = new ExternalModeConfig();
    externalModeConfig.setPort(1231);
    validConfig1.setModeConfig(externalModeConfig);
    validConfig1.setInstancePath(tempInstance);
    validConfig1.setVideoTemplatePath(tempVideoTemplate.toString());
  }

  @Test
  void test_parseArgs_valid_withValidation()
      throws ArgumentParserException, ConstraintValidationException {
    ConsumerConfig config = ConsumerCli.parseArgs(validArguments1);
    (new ConsumerConstraint()).validate(config);
    assertEquals(validConfig1, config);
  }

  @Test
  void test_parseArgs_valid_withFile() {
    assertDoesNotThrow(() -> ConsumerCli.parseArgs(fileArgument));
  }

  @Test
  void test_parseArgs_invalid_withoutInstance() throws ArgumentParserException {
    ConsumerConfig config = ConsumerCli.parseArgs(invalidArguments1);
    Constraint<ConsumerConfig> inputConstraint = new ConsumerConstraint();
    assertThrows(ConstraintValidationException.class, () -> inputConstraint.validate(config));
  }

  @Test
  void test_parseArgs_invalid_tooFewArguments() throws ArgumentParserException {
    ConsumerConfig config = ConsumerCli.parseArgs(invalidArguments2);
    Constraint<ConsumerConfig> inputConstraint = new ConsumerConstraint();
    assertThrows(ConstraintValidationException.class, () -> inputConstraint.validate(config));
  }
}