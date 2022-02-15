package edu.kit.satviz.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileExistsConstraintTest {

  private Path existingFile;
  private Path nonExistingFile;

  @BeforeEach
  void setUp() throws IOException {
    existingFile = Files.createTempFile("exists-constraint-test", null);
    nonExistingFile = Paths.get("does-not-exist-constraint-test.tmp");
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.delete(existingFile);
    existingFile = null;
    nonExistingFile = null;
  }

  @Test
  void testFileNotExists() {
    FileExistsConstraint constraint = FileExistsConstraint.fileDoesNotExist();
    assertThrows(ConstraintValidationException.class, () -> constraint.validate(existingFile));
    assertDoesNotThrow(() -> constraint.validate(nonExistingFile));
  }

  @Test
  void testFileExists() {
    FileExistsConstraint constraint = FileExistsConstraint.fileExists();
    assertThrows(ConstraintValidationException.class, () -> constraint.validate(nonExistingFile));
    assertDoesNotThrow(() -> constraint.validate(existingFile));
  }

}
