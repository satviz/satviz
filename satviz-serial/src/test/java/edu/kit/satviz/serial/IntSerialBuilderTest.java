package edu.kit.satviz.serial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class IntSerialBuilderTest {

  private IntSerializer serial = new IntSerializer();

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

    byte[] bytes = byteOut.toByteArray();
    assertEquals(4, bytes.length);

    IntSerialBuilder builder = new IntSerialBuilder();
    for (int numByte = 0; numByte < 4; numByte++) {
      assertFalse(builder.finished());
      assertFalse(builder.failed());
      assertEquals(numByte == 3, builder.addByte(bytes[numByte]));
      assertEquals(numByte == 3, builder.finished());
      if (numByte != 3) {
        assertNull(builder.getObject());
      } else {
        assertNotNull(builder.getObject());
      }
    }
    assertEquals(i, builder.getObject());
  }
}
