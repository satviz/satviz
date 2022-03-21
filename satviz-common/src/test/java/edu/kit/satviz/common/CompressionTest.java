package edu.kit.satviz.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CompressionTest {

  private static Path compressed;
  private static Path uncompressed;

  @BeforeAll
  static void setUp() throws IOException {
    compressed = extract("/comp_test.txt.xz", ".xz");
    uncompressed = extract("/comp_test.txt", ".txt");
  }

  @AfterAll
  static void tearDown() throws IOException {
    Files.delete(compressed);
    Files.delete(uncompressed);
  }

  static Path extract(String resource, String suffix) throws IOException {
    var path = Files.createTempFile(null, suffix);
    try (var stream = CompressionTest.class.getResourceAsStream(resource)) {
      Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
    }
    return path;
  }

  @Test
  void test_openPossiblyCompressed_compressed() throws IOException {
    var content = new String(Compression.openPossiblyCompressed(compressed).readAllBytes(), StandardCharsets.UTF_8);
    assertEquals("hello :)\n", content);
  }

  @Test
  void test_openPossiblyCompressed_notCompressed() throws IOException {
    var content = new String(Compression.openPossiblyCompressed(uncompressed).readAllBytes(), StandardCharsets.UTF_8);
    assertEquals("hello :)\n", content);
  }
}