package edu.kit.satviz.serial;

import org.junit.jupiter.api.Test;
import java.io.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IntSerializerTest {

  private final IntSerializer serializer = new IntSerializer();
  byte[] array = new byte[4];
  private final ByteArrayOutputStream out = new ByteArrayOutputStream(array);
  private final ByteArrayInputStream in = new ByteArrayInputStream(array);

  @Test
  void testSomeInts() {
    int[] ints = new int[]{0, 1, -1, 2, 3, 4, 5, 42, 100, 200, -3333, 1000000000, -1000000000};
    for (int i : ints) {
      assertDoesNotThrow(() -> testSerialDeserial(i));
      in.reset();
      out.reset();
    }
  }

  void testSerialDeserial(int i) throws IOException, SerializationException {
    serializer.serialize(i, out);
    assertEquals(i, serializer.deserialize(in));
  }
}
