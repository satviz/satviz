package edu.kit.satviz.serial;

import edu.kit.satviz.sat.Clause;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  void testSingleLiteral() throws IOException {
    int[] lits = new int[1];
    lits[0] = 1000000000;
    Clause c = new Clause(lits);
    serial.serialize(c, out);
    for (int i = 0; i < 7; i++) {
      System.out.println(Integer.toHexString(b[i] & 0xff));
    }
    // expect number 2000000000 encoded in 7-bit blocks
    assertEquals(b[0], (byte) (0x00 | 0x80));
    assertEquals(b[1], (byte) (0x28 | 0x80));
    assertEquals(b[2], (byte) (0x56 | 0x80));
    assertEquals(b[3], (byte) (0x39 | 0x80));
    assertEquals(b[4], (byte) 0x07);
    assertEquals(b[5], (byte) 0x00);
    assertEquals(b[6], (byte) 0x88);
  }
}
