package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReceiverTest {
  private static class IntSerializer extends Serializer<Integer> {

    @Override
    public void serialize(Integer i, OutputStream out) throws IOException {
      int _i = i;
      out.write((byte) _i);
      out.write((byte) (_i >> 8));
      out.write((byte) (_i >> 16));
      out.write((byte) (_i >> 24));
    }

    @Override
    public Integer deserialize(InputStream in) throws IOException {
      int i = in.read();
      i |= (in.read() << 8);
      i |= (in.read() << 16);
      i |= (in.read() << 24);
      return i;
    }

    @Override
    public SerialBuilder<Integer> getBuilder() {
      return new IntSerialBuilder();
    }
  }

  private static class IntSerialBuilder extends SerialBuilder<Integer> {
    private int nread = 0;
    int i = 0;

    @Override
    public boolean addByte(int inb) throws SerializationException {
      if (nread == 4)
        throw new SerializationException("done");

      i |= (inb & 0xff) << (nread++ << 3);
      return nread == 4;
    }

    @Override
    public boolean objectFinished() {
      return nread == 4;
    }

    @Override
    public Integer getObject() {
      return nread == 4 ? i : null;
    }
  }

  private static class ByteBufferOutputStream extends OutputStream {
    private final ByteBuffer bb;

    public ByteBufferOutputStream(ByteBuffer bb) {
      this.bb = bb;
    }

    @Override
    public void write(int b) throws IOException {
      try {
        bb.put((byte) b);
      } catch (BufferOverflowException | ReadOnlyBufferException e) {
        throw new IOException("bytebuffer write failed");
      }
    }
  }

  private static final byte INT_MSG_NUM = 3;
  private static NetworkBlueprint bp;
  private static final IntSerializer is = new IntSerializer();
  private Receiver r;

  @BeforeAll
  static void initAll() {
    Map<Byte, Serializer<?>> m = new HashMap<>();
    m.put(INT_MSG_NUM, new IntSerializer());
    bp = new NetworkBlueprint(m);
  }

  @BeforeEach
  void initEach() {
    r = new Receiver(bp::getBuilder);
  }

  @Test
  void testReadMultipleAtOnce() throws IOException {
    // receive data from three different integers.
    ByteBuffer bb = ByteBuffer.allocate(15);
    ByteBufferOutputStream bbOut = new ByteBufferOutputStream(bb);

    bbOut.write(INT_MSG_NUM);
    is.serialize(5, bbOut);
    bbOut.write(INT_MSG_NUM);
    is.serialize(42, bbOut);
    bbOut.write(INT_MSG_NUM);
    is.serialize(30000, bbOut);
    bb.flip();

    NetworkMessage msg;

    msg = r.receive(bb);
    assertNotNull(msg);
    assertEquals(NetworkMessage.State.PRESENT, msg.getState());
    assertEquals(INT_MSG_NUM, msg.getType());
    assertEquals(5, (Integer) msg.getObject());

    msg = r.receive(bb);
    assertNotNull(msg);
    assertEquals(NetworkMessage.State.PRESENT, msg.getState());
    assertEquals(INT_MSG_NUM, msg.getType());
    assertEquals(42, (Integer) msg.getObject());

    msg = r.receive(bb);
    assertNotNull(msg);
    assertEquals(NetworkMessage.State.PRESENT, msg.getState());
    assertEquals(INT_MSG_NUM, msg.getType());
    assertEquals(30000, (Integer) msg.getObject());

    assertNull(r.receive(bb));
  }

  @Test
  void failOnUnexpected() {
    // read a type byte that doesn't have a corresponding serializer
    ByteBuffer bb = ByteBuffer.allocate(1);
    bb.put((byte) 42);
    bb.flip();

    NetworkMessage msg = r.receive(bb);
    assertNotNull(msg);
    assertEquals(msg.getState(), NetworkMessage.State.FAIL);
  }
}