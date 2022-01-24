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
    assertEquals(b[0], 0x00 | 0x80);
    assertEquals(b[1], 0x14 | 0x80);
    assertEquals(b[2], 0x6b | 0x80);
    assertEquals(b[3], 0x5c | 0x80);
    assertEquals(b[4], 0x03);
    assertEquals(b[5], 0x00);
    assertEquals(b[6], 0x88);
  }
}
