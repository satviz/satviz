package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

class NullSerializeTest {
  private static class ByteArrayInputStream extends InputStream {
    private final byte[] b;
    private int readpos = 0;

    public ByteArrayInputStream(byte[] b) {
      this.b = b;
    }

    @Override
    public int read() {
      if (readpos == b.length) {
        return -1;
      }
      return b[readpos++];
    }
  }

  private static class ByteArrayOutputStream extends OutputStream {
    private final byte[] b;
    private int writepos = 0;

    public ByteArrayOutputStream(byte[] b) {
      this.b = b;
    }

    @Override
    public void write(int i) throws IOException {
      if (writepos == b.length) {
        throw new IOException("stream finished");
      }
      b[writepos++] = (byte) i;
    }
  }

  private static final NullSerializer ns = new NullSerializer();

  @Test
  void nullSerializeWorks() throws IOException {
    byte[] b = new byte[8];
    b[0] = (byte) 0x88; // some random value
    b[1] = (byte) 0x88;
    ns.serialize(null, new ByteArrayOutputStream(b));
    assertEquals(b[0], (byte) 0);
    assertEquals(b[1], (byte) 0x88); // make sure we only write one byte
  }

  @Test
  void nullDeserializeWorks() throws IOException, SerializationException {
    byte[] b = new byte[8];
    assertNull(ns.deserialize(new ByteArrayInputStream(b)));
  }

  @Test
  void nullDeserializeCatchesWrongData() {
    byte[] b = new byte[8];
    b[0] = (byte) 0x88; // some random value
    assertThrows(SerializationException.class, () -> ns.deserialize(new ByteArrayInputStream(b)));
  }

  @Test
  void nullBuilderWorks() throws SerializationException {
    SerialBuilder<Object> nb = ns.getBuilder();
    assertTrue(nb.addByte(0));
    assertThrows(SerializationException.class, () -> nb.addByte(0));
  }
}
