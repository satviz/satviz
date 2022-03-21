package edu.kit.satviz.consumer.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsumerCliTest {

  private ConsumerConfig validConfig1;
  private String[] arguments1;
  private String[] invalidArguments1;

  @BeforeEach
  void setUp() throws IOException {
    Path tempInstance = Files.createTempFile("instance",".cnf");
    Path tempVideoTemplate = Files.createTempFile("video", ".ogv");
    arguments1 = new String[] {
        "-i", tempInstance.toString(), "-o", tempVideoTemplate.toString(), "external", "-P", "1231"
    };
    invalidArguments1 = new String[] {
        "external", "-P", "1231"
    };
    validConfig1 = new ConsumerConfig();
    ExternalModeConfig externalModeConfig = new ExternalModeConfig();
    externalModeConfig.setPort(1231);
    validConfig1.setModeConfig(externalModeConfig);
    validConfig1.setInstancePath(tempInstance);
    validConfig1.setVideoTemplatePath(tempVideoTemplate.toString());
  }

  @Test
  void test_parseArgs_validArgumentsWithValidation()
      throws ArgumentParserException, ConstraintValidationException {
    ConsumerConfig config = ConsumerCli.parseArgs(arguments1);
    (new ConsumerConstraint()).validate(config);
    assertEquals(validConfig1, config);
  }

  @Test
  void test_parseArgs_withoutInstance() throws ArgumentParserException {
    ConsumerConfig config = ConsumerCli.parseArgs(invalidArguments1);
    Constraint<ConsumerConfig> inputConstraint = new ConsumerConstraint();
    assertThrows(ConstraintValidationException.class, () -> inputConstraint.validate(config));
  }
}