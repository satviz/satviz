package edu.kit.satviz.serial;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class StringTest {

  private final StringSerializer serial = new StringSerializer();

  @Test
  void testSimpleAscii() throws IOException {
    try {
      testSingleString("Hello, World!\n\nHow are you?");
    } catch (SerializationException e) {
      fail(e);
    }
  }

  @Test
  void testUnicode() throws IOException {
    try {
      testSingleString("༼ つ ◕_◕ ༽つ");
    } catch (SerializationException e) {
      fail(e);
    }
  }

  void testSingleString(String s) throws IOException, SerializationException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    serial.serialize(s, byteOut);

    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
    assertEquals(s, serial.deserialize(byteIn));
  }
}
