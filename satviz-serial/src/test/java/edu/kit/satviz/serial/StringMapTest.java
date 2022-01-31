package edu.kit.satviz.serial;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StringMapTest {

  private final StringMapSerializer serial = new StringMapSerializer();

  @Test
  void testSimpleAscii() {
    Map<String, String> map = new HashMap<>();
    map.put("Hello", "World!");
    map.put("Weather", "Sunny");
    map.put("...", "...?");
    assertDoesNotThrow(() -> testSingleMap(map));
  }

  @Test
  void testEscape() {
    Map<String, String> map = new HashMap<>();
    map.put("a==b", "c==d");
    map.put("Hello\nWorld", "How\nare\nyou?");
    map.put("int i =", "5");
    assertDoesNotThrow(() -> testSingleMap(map));
  }

  @Test
  void testUnicode() {
    Map<String, String> map = new HashMap<>();
    map.put("༼ つ  ͡° ͜ʖ ͡° ༽つ", "༼ つ ಥ_ಥ ༽つ");
    map.put("こんにちは", "नमस्ते");
    assertDoesNotThrow(() -> testSingleMap(map));
  }

  void testSingleMap(Map<String, String> map) throws IOException, SerializationException {
    byte[] bytes = new byte[map.size() * 128]; // random upper bound
    ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream(bytes);

    serial.serialize(map, byteOut);
    Map<String, String> result = serial.deserialize(byteIn);

    assertEquals(map.size(), result.size());
    for (Map.Entry<String, String> entry : result.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      assertEquals(map.get(key), value);
    }
  }
}
