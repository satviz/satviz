package edu.kit.satviz.consumer.processing;

import static org.junit.jupiter.api.Assertions.*;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.ClauseUpdate.Type;
import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExternalClauseBufferTest {

  private static final ClauseUpdate EXAMPLE_UPDATE =
      new ClauseUpdate(new Clause(new int[] {1, 2, -3}), Type.ADD);

  private static final ClauseUpdate[] UPDATES = {
      ClauseUpdate.of(Type.ADD, 3, -2, 6),
      ClauseUpdate.of(Type.ADD, 1),
      ClauseUpdate.of(Type.ADD, 5, 4, 1, 2, -3),
      ClauseUpdate.of(Type.REMOVE, 3, 8, 4, -6),
      ClauseUpdate.of(Type.REMOVE, 2, 1)
  };

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
    buffer.close();
    for (Path path : Files.list(testDir).toList()) {
      Files.delete(path);
    }
   Files.delete(testDir);
  }

  @Test
  void test_addClauseUpdate_multiple() throws IOException {
    addUpdates();
  }

  @Test
  void test_addClauseUpdate_single() throws IOException {
    buffer.addClauseUpdate(EXAMPLE_UPDATE);
    assertEquals(1, buffer.size());
  }

  @Test
  void test_getClauseUpdates_outOfBounds() {
    assertThrows(IndexOutOfBoundsException.class, () -> buffer.getClauseUpdates(0, 1));
    assertThrows(IndexOutOfBoundsException.class, () -> buffer.getClauseUpdates(2, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> buffer.getClauseUpdates(-1, 0));
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
  void test_getClauseUpdates_multiple() throws IOException, SerializationException {
    addUpdates();
    var result = buffer.getClauseUpdates(1, 3);
    var expected = Arrays.copyOfRange(UPDATES, 1, 4);
    assertArrayEquals(expected, result);
  }

  @Test
  void test_getClauseUpdates_tooMany() throws IOException, SerializationException {
    addUpdates();
    var result = buffer.getClauseUpdates(2, 10);
    var expected = Arrays.copyOfRange(UPDATES, 2, UPDATES.length);
    assertArrayEquals(expected, result);
  }

  @Test
  void test_close() throws IOException, SerializationException {
    buffer.addClauseUpdate(EXAMPLE_UPDATE);
    var x = buffer.getClauseUpdates(0, 1);
    buffer.close();
    assertThrows(IOException.class, () -> buffer.getClauseUpdates(0, 1));
    //assertThrows(IOException.class, () -> buffer.addClauseUpdate(EXAMPLE_UPDATE));
  }

  private void addUpdates() throws IOException {
    for (var update : UPDATES) {
      buffer.addClauseUpdate(update);
    }
    assertEquals(UPDATES.length, buffer.size());
  }
}