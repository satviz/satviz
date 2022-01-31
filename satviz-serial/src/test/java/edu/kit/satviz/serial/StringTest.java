package edu.kit.satviz.serial;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StringTest {

  private final StringSerializer serial = new StringSerializer();

  @Test
  void testSimpleAscii() {
    assertDoesNotThrow(() -> testSingleString("Hello, World!\n\nHow are you?"));
  }

  @Test
  void testUnicode() {
    assertDoesNotThrow(() -> testSingleString("༼ つ ◕_◕ ༽つ"));
  }

  void testSingleString(String s) throws IOException, SerializationException {
    byte[] bytes = new byte[s.length() * 8]; // quick upper bound
    ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream(bytes);

    serial.serialize(s, byteOut);
    assertEquals(s, serial.deserialize(byteIn));
  }
}
