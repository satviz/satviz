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

  private Path tempInstance;
  private Path tempVideoTemplate;

  @BeforeEach
  void setUp() throws IOException {
    tempInstance = Files.createTempFile("instance", ".cnf");
    tempVideoTemplate = Files.createTempFile("video", ".ogv");
  }

  private ConsumerConfig getExternalConfig() {
    var config = new ConsumerConfig();
    var externalModeConfig = new ExternalModeConfig();
    externalModeConfig.setPort(1231);
    config.setModeConfig(externalModeConfig);
    config.setInstancePath(tempInstance);
    config.setVideoTemplatePath(tempVideoTemplate.toString());
    return config;
  }

  private ConsumerConfig getEmbeddedConfig(
      EmbeddedModeSource sourceMode, Path source
  ) {
    var config = new ConsumerConfig();
    var embeddedModeConfig = new EmbeddedModeConfig();
    embeddedModeConfig.setSource(sourceMode);
    embeddedModeConfig.setSourcePath(source);
    config.setModeConfig(embeddedModeConfig);
    config.setInstancePath(tempInstance);
    return config;
  }

  @Test
  void test_parseArgs_valid_external()
      throws ArgumentParserException, ConstraintValidationException {
    var arguments = new String[] {
        "-i", tempInstance.toString(),
        "-o", tempVideoTemplate.toString(),
        "external", "-P", "1231"
    };
    var config = getExternalConfig();
    var result = ConsumerCli.parseArgs(arguments);
    (new ConsumerConstraint()).validate(result);
    assertEquals(config, result);
  }

  @Test
  void test_parseArgs_valid_embeddedSolver()
      throws ArgumentParserException, ConstraintValidationException, IOException {
    Path tempSolver = Files.createTempFile("solver", ".so");
    var arguments = new String[] {
        "-i", tempInstance.toString(),
        "embedded", "-s", tempSolver.toString()
    };
    var config = getEmbeddedConfig(EmbeddedModeSource.SOLVER, tempSolver);
    var result = ConsumerCli.parseArgs(arguments);
    (new ConsumerConstraint()).validate(result);
    assertEquals(config, result);
  }

  @Test
  void test_parseArgs_valid_embeddedProof()
      throws ArgumentParserException, ConstraintValidationException, IOException {
    Path tempProof = Files.createTempFile("proof", ".drat");
    var arguments = new String[] {
        "-i", tempInstance.toString(),
        "embedded", "-p", tempProof.toString()
    };
    var config = getEmbeddedConfig(EmbeddedModeSource.PROOF, tempProof);
    var result = ConsumerCli.parseArgs(arguments);
    (new ConsumerConstraint()).validate(result);
    assertEquals(config, result);
  }

  @Test
  void test_parseArgs_valid_withFile() throws IOException {
    Path configPath = Files.createTempFile("config", ".json");
    Files.copy(
        Objects.requireNonNull(ConsumerCliTest.class.getResourceAsStream("/config2.json")),
        configPath,
        StandardCopyOption.REPLACE_EXISTING
    );
    var arguments = new String[] {"config", configPath.toString()};
    assertDoesNotThrow(() -> ConsumerCli.parseArgs(arguments));
  }

  @Test
  void test_parseArgs_valid_colors()
      throws ArgumentParserException, ConstraintValidationException {
    var arguments = new String[] {
        "-i", tempInstance.toString(),
        "-c", "#000000:#FFFFFF",
        "-o", tempVideoTemplate.toString(),
        "external", "-P", "1231"
    };
    var config = getExternalConfig();
    HeatmapColors heatmapColors = new HeatmapColors();
    heatmapColors.setFromColor(0x000000);
    heatmapColors.setToColor(0xffffff);
    config.setHeatmapColors(heatmapColors);
    var result = ConsumerCli.parseArgs(arguments);
    (new ConsumerConstraint()).validate(result);
    assertEquals(config, result);
  }

  @Test
  void test_parseArgs_invalid_withoutInstance() throws ArgumentParserException {
    var arguments = new String[] {"external", "-P", "1231"};
    var config = ConsumerCli.parseArgs(arguments);
    Constraint<ConsumerConfig> inputConstraint = new ConsumerConstraint();
    assertThrows(ConstraintValidationException.class, () -> inputConstraint.validate(config));
  }

  @Test
  void test_parseArgs_invalid_tooFewArguments() throws ArgumentParserException {
    var arguments = new String[] {"embedded"};
    var config = ConsumerCli.parseArgs(arguments);
    Constraint<ConsumerConfig> inputConstraint = new ConsumerConstraint();
    assertThrows(ConstraintValidationException.class, () -> inputConstraint.validate(config));
  }

  @Test
  void test_parseArgs_invalidColors() {
    var arguments = new String[] {
        "-i", tempInstance.toString(), "-c", "000000:#FFFFFF", "external", "-P", "1231"
    };
    assertThrows(ArgumentParserException.class, () -> ConsumerCli.parseArgs(arguments));
  }

}