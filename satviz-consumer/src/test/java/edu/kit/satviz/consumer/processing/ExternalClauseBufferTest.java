package edu.kit.satviz.consumer.processing;

import static org.junit.jupiter.api.Assertions.*;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExternalClauseBufferTest {

  private static final ClauseUpdate EXAMPLE_UPDATE =
      new ClauseUpdate(new Clause(new int[] {1, 2, -3}), ClauseUpdate.Type.ADD);

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

  // TODO: 10/02/2022 add/get with multiple updates of different sizes

  @Test
  void test_addClauseUpdate_single() throws IOException {
    buffer.addClauseUpdate(EXAMPLE_UPDATE);
    assertEquals(1, buffer.size());
  }

  @Test
  void test_getClauseUpdates_tooFar() {
    assertThrows(IndexOutOfBoundsException.class, () -> buffer.getClauseUpdates(0, 1));
    assertThrows(IndexOutOfBoundsException.class, () -> buffer.getClauseUpdates(2, 0));
  }

  @Test
  void test_getClauseUpdates_negativeAmount() throws IOException {
    buffer.addClauseUpdate(EXAMPLE_UPDATE);
    assertEquals(1, buffer.size());
    assertThrows(IllegalArgumentException.class, () -> buffer.getClauseUpdates(0, -1));
  }

  @Test
  void test_getClauseUpdates_zero() throws SerializationException, IOException {
    buffer.addClauseUpdate(EXAMPLE_UPDATE);
    assertEquals(1, buffer.size());
    var result = buffer.getClauseUpdates(0, 0);
    assertEquals(0, result.length);
  }

  @Test
  void test_getClauseUpdates_single() throws IOException, SerializationException {
    buffer.addClauseUpdate(EXAMPLE_UPDATE);
    assertEquals(1, buffer.size());
    var result = buffer.getClauseUpdates(0, 1);
    assertArrayEquals(new ClauseUpdate[] { EXAMPLE_UPDATE }, result);
  }

  @Test
  void test_close() throws IOException, SerializationException {
    buffer.addClauseUpdate(EXAMPLE_UPDATE);
    var x = buffer.getClauseUpdates(0, 1);
    buffer.close();
    assertThrows(IOException.class, () -> buffer.getClauseUpdates(0, 1));
    //assertThrows(IOException.class, () -> buffer.addClauseUpdate(EXAMPLE_UPDATE));
  }
}