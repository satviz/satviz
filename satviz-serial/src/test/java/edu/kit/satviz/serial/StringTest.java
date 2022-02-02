package edu.kit.satviz.serial;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    serial.serialize(s, byteOut);

    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
    assertEquals(s, serial.deserialize(byteIn));
  }
}
