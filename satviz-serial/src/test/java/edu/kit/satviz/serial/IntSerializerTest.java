package edu.kit.satviz.serial;

import org.junit.jupiter.api.Test;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class IntSerializerTest {

  private final IntSerializer serial = new IntSerializer();

  @Test
  void testSomeInts() throws IOException {
    int[] ints = new int[]{0, 1, -1, 2, 3, 4, 5, 42, 100, 200, -3333, 1000000000, -1000000000};
    for (int i : ints) {
      try {
        testSingleInt(i);
      } catch (SerializationException e) {
        fail(e);
      }
    }
  }

  void testSingleInt(int i) throws IOException, SerializationException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    serial.serialize(i, byteOut);

    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
    Integer result = serial.deserialize(byteIn);

    assertEquals(i, result);
  }
}
