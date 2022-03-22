package edu.kit.satviz.producer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProducerApplicationTest {

  private static Path proofFile;

  @BeforeAll
  static void setUp() throws IOException {
    ResourceHelper.createTempDir();
    proofFile = ResourceHelper.extractResource("/proof.drat");
  }

  @AfterAll
  static void tearDown() throws IOException {
    ResourceHelper.deleteTempDir();
  }

  @Test
  void test_main_normal() {
    String[] args = {"--proof", proofFile.toString(), "-H", "localhost"};
    assertDoesNotThrow(() -> ProducerApplication.main(args));
  }
}