package edu.kit.satviz.serial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class IntSerialBuilderTest {

  private IntSerializer serial = new IntSerializer();

  @Test
  void testSomeInts() {
    int[] ints = new int[]{0, 1, -1, 2, 3, 4, 5, 42, 100, 200, -3333, 1000000000, -1000000000};
    for (int i : ints) {
      assertDoesNotThrow(() -> testSingleInt(i));
    }
  }

  void testSingleInt(int i) throws IOException, SerializationException {
    byte[] bytes = new byte[4];
    serial.serialize(i, new ByteArrayOutputStream(bytes));

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
