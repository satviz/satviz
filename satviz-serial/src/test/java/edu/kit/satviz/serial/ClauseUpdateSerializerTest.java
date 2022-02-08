package edu.kit.satviz.serial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClauseUpdateSerializerTest {

  private ClauseUpdateSerializer serializer;
  private ClauseUpdate trivialUpdate;
  private ClauseUpdate update;

  @BeforeEach
  void setUp() {
    serializer = new ClauseUpdateSerializer();
    trivialUpdate = new ClauseUpdate(new Clause(new int[0]), ClauseUpdate.Type.ADD);
    update = new ClauseUpdate(new Clause(new int[] {1, 2, -4, 6}), ClauseUpdate.Type.REMOVE);
  }

  @Test
  void testSerialize() throws IOException {
    var out = new ByteArrayOutputStream();
    try {
      serializer.serialize(trivialUpdate, out);
    } catch (SerializationException e) {
      fail(e);
    }
    assertEquals(ClauseUpdate.Type.ADD.ordinal(), out.toByteArray()[0]);
  }

  @Test
  void testLoopback() throws IOException {
    var out = new ByteArrayOutputStream();
    try {
      serializer.serialize(update, out);
    } catch (SerializationException e) {
      fail(e);
    }
    var in = new ByteArrayInputStream(out.toByteArray());
    try {
      assertEquals(update, serializer.deserialize(in));
    } catch (SerializationException e) {
      fail(e);
    }
  }

}
