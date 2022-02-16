package edu.kit.satviz.serial;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.SatAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class ClauseTest {
  ClauseSerializer serial = new ClauseSerializer();

  @Test
  void testSingleLiteral() throws IOException {
    int[] lits = new int[1];
    lits[0] = 1000000000;
    Clause c = new Clause(lits);
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

    serial.serialize(c, byteOut);
    byte[] b = byteOut.toByteArray();
    assertEquals(6, b.length);

    // expect number 2000000000 encoded in 7-bit blocks
    assertEquals((byte) (0x00 | 0x80), b[0]);
    assertEquals((byte) (0x28 | 0x80), b[1]);
    assertEquals((byte) (0x56 | 0x80), b[2]);
    assertEquals((byte) (0x39 | 0x80), b[3]);
    assertEquals((byte) 0x07, b[4]);
    assertEquals((byte) 0x00, b[5]);

    ByteArrayInputStream byteIn = new ByteArrayInputStream(b);
    Clause result = null;
    try {
      result = serial.deserialize(byteIn);
    } catch (SerializationException e) {
      fail(e);
    }
    assertEquals(1, result.literals().length);
    assertEquals(result.literals()[0], lits[0]);
  }

  @Test
  void testClauses() throws IOException {
    try {
      testClause(new int[]{1, 2, 3, 4, 5, 6});
      testClause(new int[]{-20, 10, 10, 20});
      testClause(new int[0]);
      testClause(new int[]{-1000000, 1000000});
      testClause(new int[]{-1, 1, -2, 2, -3, 3, -4, 4, -5, 5});
    } catch (SerializationException e) {
      fail(e);
    }
  }

  void testClause(int[] lits) throws IOException, SerializationException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    serial.serialize(new Clause(lits), byteOut);

    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
    Clause result = serial.deserialize(byteIn);

    assertArrayEquals(lits, result.literals());
  }
}
