package edu.kit.satviz.serial;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StringMapTest {

  private final StringMapSerializer serial = new StringMapSerializer();

  @Test
  void testSimpleAscii() throws IOException {
    Map<String, String> map = new HashMap<>();
    map.put("Hello", "World!");
    map.put("Weather", "Sunny");
    map.put("...", "...?");
    try {
      testSingleMap(map);
    } catch (SerializationException e) {
      fail(e);
    }
  }

  @Test
  void testEscape() throws IOException {
    Map<String, String> map = new HashMap<>();
    map.put("a==b", "c==d");
    map.put("Hello\nWorld", "How\nare\nyou?");
    map.put("int i =", "5");
    map.put("\\ \\", "\\ \\");
    try {
      testSingleMap(map);
    } catch (SerializationException e) {
      fail(e);
    }
  }

  @Test
  void testUnicode() throws IOException {
    Map<String, String> map = new HashMap<>();
    map.put("༼ つ  ͡° ͜ʖ ͡° ༽つ", "༼ つ ಥ_ಥ ༽つ");
    map.put("こんにちは", "नमस्ते");
    try {
      testSingleMap(map);
    } catch (SerializationException e) {
      fail(e);
    }
  }

  void testSingleMap(Map<String, String> map) throws IOException, SerializationException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    serial.serialize(map, byteOut);

    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
    Map<String, String> result = serial.deserialize(byteIn);

    assertEquals(map.size(), result.size());
    for (Map.Entry<String, String> entry : result.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      assertEquals(map.get(key), value);
    }
  }

  @Test
  void testReset() {
    StringMapSerialBuilder builder = new StringMapSerialBuilder();
    byte[] mapping = new byte[]{'b', '=', '2'};
    try {
      assertFalse(builder.addByte((byte) 'a'));
      // don't want to use this character
      builder.reset();
      for (byte b : mapping) {
        assertFalse(builder.addByte(b));
      }
      assertTrue(builder.addByte((byte) '\0'));

      Map<String, String> m = builder.getObject();
      assertNotNull(m);
      assertEquals(1, m.size());
      assertEquals("2", m.get("b"));

    } catch (SerializationException e) {
      fail(e);
    }
  }
}
