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
  void testSingleLiteral() throws IOException, SerializationException {
    int[] lits = new int[1];
    lits[0] = 1000000000;
    Clause c = new Clause(lits);
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

    serial.serialize(c, byteOut);
    byte[] b = byteOut.toByteArray();
    assertEquals(6, b.length);

    // expect number 2000000000 encoded in 7-bit blocks
    assertEquals(b[0], (byte) (0x00 | 0x80));
    assertEquals(b[1], (byte) (0x28 | 0x80));
    assertEquals(b[2], (byte) (0x56 | 0x80));
    assertEquals(b[3], (byte) (0x39 | 0x80));
    assertEquals(b[4], (byte) 0x07);
    assertEquals(b[5], (byte) 0x00);

    ByteArrayInputStream byteIn = new ByteArrayInputStream(b);
    Clause cNew = serial.deserialize(byteIn);
    assertEquals(1, cNew.literals().length);
    assertEquals(cNew.literals()[0], lits[0]);
  }

  @Test
  void testClauses() throws SerializationException, IOException {
    testClause(new int[]{1,2,3,4,5,6});
    testClause(new int[]{-20, 10, 10, 20});
    testClause(new int[0]);
    testClause(new int[]{-1000000, 1000000});
    testClause(new int[]{-1, 1, -2, 2, -3, 3, -4, 4, -5, 5});
  }

  void testClause(int[] lits) throws IOException, SerializationException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    serial.serialize(new Clause(lits), byteOut);

    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
    Clause result = serial.deserialize(byteIn);

    assertArrayEquals(lits, result.literals());
  }
}
