package edu.kit.satviz.producer;

import edu.kit.satviz.common.Constraint;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.producer.cli.ProducerConstraints;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.mode.ProofMode;
import edu.kit.satviz.producer.mode.SolverMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProducerConstraintsTest {

  private Constraint<ProducerParameters> constraint;
  private Path existingFile;
  private Path notExistingFile;

  @BeforeEach
  void setUp() throws IOException {
    constraint = ProducerConstraints.paramConstraints(List.of(new ProofMode(), new SolverMode()));
    existingFile = Files.createTempFile("producer-constraint", null);
    notExistingFile = Paths.get("abcdef123.tmp");
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.delete(existingFile);
  }

  @Test
  void test_validate_multipleModes() {
    var params = new ProducerParameters();
    params.setSolverFile(existingFile);
    params.setInstanceFile(existingFile);
    params.setProofFile(existingFile);
    params.setHost("example.com");
    assertThrows(ConstraintValidationException.class, () -> constraint.validate(params));
  }

  @Test
  void test_validate_noMode() {
    var params = new ProducerParameters();
    params.setHost("example.com");
    assertThrows(ConstraintValidationException.class, () -> constraint.validate(params));
  }

  @Test
  void test_validate_proofDoesNotExist() {
    var params = new ProducerParameters();
    params.setProofFile(notExistingFile);
    params.setHost("example.com");
    assertThrows(ConstraintValidationException.class, () -> constraint.validate(params));
  }

  @Test
  void test_validate_solverDoesNotExist() {
    var params = new ProducerParameters();
    params.setSolverFile(notExistingFile);
    params.setInstanceFile(existingFile);
    params.setHost("example.com");
    assertThrows(ConstraintValidationException.class, () -> constraint.validate(params));
  }

  @Test
  void test_validate_instanceDoesNotExist() {
    var params = new ProducerParameters();
    params.setSolverFile(existingFile);
    params.setInstanceFile(notExistingFile);
    params.setHost("example.com");
    assertThrows(ConstraintValidationException.class, () -> constraint.validate(params));
  }

  @Test
  void test_validate_okSolverMode() {
    var params = new ProducerParameters();
    params.setSolverFile(existingFile);
    params.setInstanceFile(existingFile);
    params.setHost("example.com");
    assertDoesNotThrow(() -> constraint.validate(params));
  }

  @Test
  void test_validate_okProofMode() {
    var params = new ProducerParameters();
    params.setProofFile(existingFile);
    params.setHost("example.com");
    assertDoesNotThrow(() -> constraint.validate(params));
  }

}
