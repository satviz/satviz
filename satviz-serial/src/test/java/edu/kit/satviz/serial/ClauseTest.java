package edu.kit.satviz.serial;

import edu.kit.satviz.sat.Clause;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClauseTest {

  private byte[] b;
  ByteArrayInputStream in;
  ByteArrayOutputStream out;
  ClauseSerializer serial = new ClauseSerializer();

  @BeforeEach
  void init() {
    b = new byte[128];
    for (int i = 0; i < 128; i++) {
      b[i] = (byte) 0x88; // dummy value
    }
    in = new ByteArrayInputStream(b);
    out = new ByteArrayOutputStream(b);
  }

  @Test
  void testSingleLiteral() throws IOException, SerializationException {
    int[] lits = new int[1];
    lits[0] = 1000000000;
    Clause c = new Clause(lits);
    serial.serialize(c, out);
    // expect number 2000000000 encoded in 7-bit blocks
    assertEquals(b[0], (byte) (0x00 | 0x80));
    assertEquals(b[1], (byte) (0x28 | 0x80));
    assertEquals(b[2], (byte) (0x56 | 0x80));
    assertEquals(b[3], (byte) (0x39 | 0x80));
    assertEquals(b[4], (byte) 0x07);
    assertEquals(b[5], (byte) 0x00);
    assertEquals(b[6], (byte) 0x88);

    Clause cNew = serial.deserialize(in);
    assertTrue(cNew.literals().length == 1);
    assertTrue(cNew.literals()[0] == lits[0]);
  }

  @Test
  void testClauses() throws SerializationException, IOException {
    testClause(new int[]{1,2,3,4,5,6});
    testClause(new int[]{-20, 10, 10, 20});
    testClause(new int[0]);
    testClause(new int[]{-1000000, 1000000});
  }

  void testClause(int[] lits) throws IOException, SerializationException {
    serial.serialize(new Clause(lits), out);
    Clause cNew = serial.deserialize(in);
    assertTrue(Arrays.equals(cNew.literals(), lits));
  }
}
