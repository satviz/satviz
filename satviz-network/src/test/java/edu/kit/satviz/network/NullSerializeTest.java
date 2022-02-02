package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class NullSerializeTest {

  private static final NullSerializer serial = new NullSerializer();

  @Test
  void nullSerializeWorks() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try {
      serial.serialize(null, byteOut);
    } catch (SerializationException e) {
      fail();
    }

    byte[] bytes = byteOut.toByteArray();
    assertEquals(1, bytes.length);
    assertEquals(0, bytes[0]);
  }

  @Test
  void nullDeserializeWorks() throws IOException, SerializationException {
    byte[] b = new byte[1];
    assertNull(serial.deserialize(new ByteArrayInputStream(b)));
  }

  @Test
  void nullDeserializeCatchesWrongData() {
    byte[] b = new byte[1];
    b[0] = (byte) 0x88; // some random value
    ByteArrayInputStream byteIn = new ByteArrayInputStream(b);
    assertThrows(SerializationException.class, () -> serial.deserialize(byteIn));
  }

  @Test
  void nullBuilderWorks() throws SerializationException {
    NullSerialBuilder builder = new NullSerialBuilder();
    assertTrue(builder.addByte((byte) 0));
    assertThrows(SerializationException.class, () -> builder.addByte((byte) 0));

    builder.reset();
    assertThrows(SerializationException.class, () -> builder.addByte((byte) 0x88));
  }
}
