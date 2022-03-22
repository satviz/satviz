package edu.kit.satviz.consumer.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ConsumerConstraint;
import edu.kit.satviz.consumer.config.EmbeddedModeConfig;
import edu.kit.satviz.consumer.config.EmbeddedModeSource;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import edu.kit.satviz.consumer.config.HeatmapColors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsumerCliTest {

  private ConsumerConfig validConfig1;
  private ConsumerConfig validConfig2;
  private ConsumerConfig validConfig3;
  private ConsumerConfig validConfig4;
  private String[] externalArguments;
  private String[] embeddedSolverArguments;
  private String[] embeddedProofArguments;
  private String[] fileArgument;
  private String[] colorArguments;
  private String[] invalidArguments1;
  private String[] invalidArguments2;
  private String[] invalidArguments3;

  @BeforeEach
  void setUp() throws IOException {
    Path tempInstance = Files.createTempFile("instance", ".cnf");
    Path tempVideoTemplate = Files.createTempFile("video", ".ogv");
    Path configPath = Files.createTempFile("config", ".json");
    Files.copy(
        Objects.requireNonNull(ConsumerCliTest.class.getResourceAsStream("/config2.json")),
        configPath,
        StandardCopyOption.REPLACE_EXISTING
    );

    externalArguments = new String[] {
        "-i", tempInstance.toString(),
        "-o", tempVideoTemplate.toString(),
        "external", "-P", "1231"
    };
    Path tempSolver = Files.createTempFile("solver", ".so");
    embeddedSolverArguments = new String[] {
        "-i", tempInstance.toString(),
        "embedded", "-s", tempSolver.toString()
    };
    Path tempProof = Files.createTempFile("proof", ".drat");
    embeddedProofArguments = new String[] {
        "-i", tempInstance.toString(),
        "embedded", "-p", tempProof.toString()
    };
    fileArgument = new String[] {"config", configPath.toString()};
    colorArguments = new String[] {
        "-i", tempInstance.toString(),
        "-c", "#000000:#FFFFFF",
        "-o", tempVideoTemplate.toString(),
        "external", "-P", "1231"
    };
    invalidArguments1 = new String[] {"external", "-P", "1231"};
    invalidArguments2 = new String[] {"embedded"};
    invalidArguments3 = new String[] {
        "-i", tempInstance.toString(), "-c", "000000:#FFFFFF", "external", "-P", "1231"
    };

    validConfig1 = getExternalConfig(tempInstance, tempVideoTemplate);
    validConfig2 = getEmbeddedConfig(tempInstance, EmbeddedModeSource.SOLVER, tempSolver);
    validConfig3 = getEmbeddedConfig(tempInstance, EmbeddedModeSource.PROOF, tempProof);
    validConfig4 = getExternalConfig(tempInstance, tempVideoTemplate);
    HeatmapColors heatmapColors = new HeatmapColors();
    heatmapColors.setFromColor(0x000000);
    heatmapColors.setToColor(0xffffff);
    validConfig4.setHeatmapColors(heatmapColors);
  }

  private ConsumerConfig getExternalConfig(Path instance, Path tempVideoTemplate) {
    var config = new ConsumerConfig();
    var externalModeConfig = new ExternalModeConfig();
    externalModeConfig.setPort(1231);
    config.setModeConfig(externalModeConfig);
    config.setInstancePath(instance);
    config.setVideoTemplatePath(tempVideoTemplate.toString());
    return config;
  }

  private ConsumerConfig getEmbeddedConfig(
      Path instance, EmbeddedModeSource sourceMode, Path source
  ) {
    var config = new ConsumerConfig();
    var embeddedModeConfig = new EmbeddedModeConfig();
    embeddedModeConfig.setSource(sourceMode);
    embeddedModeConfig.setSourcePath(source);
    config.setModeConfig(embeddedModeConfig);
    config.setInstancePath(instance);
    return config;
  }

  @Test
  void test_parseArgs_valid_external()
      throws ArgumentParserException, ConstraintValidationException {
    ConsumerConfig config = ConsumerCli.parseArgs(externalArguments);
    (new ConsumerConstraint()).validate(config);
    assertEquals(validConfig1, config);
  }

  @Test
  void test_parseArgs_valid_embeddedSolver()
      throws ArgumentParserException, ConstraintValidationException {
    ConsumerConfig config = ConsumerCli.parseArgs(embeddedSolverArguments);
    (new ConsumerConstraint()).validate(config);
    assertEquals(validConfig2, config);
  }

  @Test
  void test_parseArgs_valid_embeddedProof()
      throws ArgumentParserException, ConstraintValidationException {
    ConsumerConfig config = ConsumerCli.parseArgs(embeddedProofArguments);
    (new ConsumerConstraint()).validate(config);
    assertEquals(validConfig3, config);
  }

  @Test
  void test_parseArgs_valid_withFile() {
    assertDoesNotThrow(() -> ConsumerCli.parseArgs(fileArgument));
  }

  @Test
  void test_parseArgs_valid_colors()
      throws ArgumentParserException, ConstraintValidationException {
    ConsumerConfig config = ConsumerCli.parseArgs(colorArguments);
    (new ConsumerConstraint()).validate(config);
    assertEquals(validConfig4, config);
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

  @Test
  void test_parseArgs_invalidColors() {
    assertThrows(ArgumentParserException.class, () -> ConsumerCli.parseArgs(invalidArguments3));
  }

}