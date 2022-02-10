package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExternalClauseBufferTest {

  private Path testDir;
  private ExternalClauseBuffer buffer;

  @BeforeEach
  void setUp() throws IOException {
    Path parent = Paths.get("build/test-files");
    Files.createDirectories(parent);
    testDir = Files.createTempDirectory(parent, "buffer-test");
    buffer = new ExternalClauseBuffer(testDir);
  }

  @AfterEach
  void tearDownTestDir() throws IOException {
    for (Path path : Files.list(testDir).toList()) {
      Files.delete(path);
    }
    Files.delete(testDir);
  }

  @Test
  void test_addClauseUpdate() {
  }

  @Test
  void test_getClauseUpdates_tooFar() {
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> )buffer.getClauseUpdates(0, 1);
  }

  @Test
  void getClauseUpdates() {
  }
}