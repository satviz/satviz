package edu.kit.satviz.network;

import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReceiverTest {
  /*

  private static final byte INT_MSG_NUM = 3;
  private static NetworkBlueprint bp;
  private static final IntSerializer serial = new IntSerializer();
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
    serial.serialize(5, bbOut);
    bbOut.write(INT_MSG_NUM);
    serial.serialize(42, bbOut);
    bbOut.write(INT_MSG_NUM);
    serial.serialize(30000, bbOut);
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
  void testInterrupted() throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(5);
    ByteBufferOutputStream bbOut = new ByteBufferOutputStream(bb);

    bbOut.write(INT_MSG_NUM);
    serial.serialize(123456789, bbOut);
    bb.flip();

    ByteBuffer bb1 = ByteBuffer.allocate(3);
    ByteBuffer bb2 = ByteBuffer.allocate(2);
    bb1.put(bb.get());
    bb1.put(bb.get());
    bb1.put(bb.get());
    bb2.put(bb.get());
    bb2.put(bb.get());
    bb1.flip();
    bb2.flip();

    NetworkMessage msg;
    msg = r.receive(bb1);
    assertNull(msg);
    assertFalse(bb1.hasRemaining());
    msg = r.receive(bb2);
    assertNotNull(msg);
    assertFalse(bb2.hasRemaining());

    assertEquals(NetworkMessage.State.PRESENT, msg.getState());
    assertEquals(INT_MSG_NUM, msg.getType());
    assertEquals(123456789, (Integer) msg.getObject());
  }

  @Test
  void failOnUnexpected() {
    // read a type byte that doesn't have a corresponding serializer
    ByteBuffer bb = ByteBuffer.allocate(1);
    bb.put((byte) 42);
    bb.flip();

    NetworkMessage msg = r.receive(bb);
    assertNotNull(msg);
    assertEquals(NetworkMessage.State.FAIL, msg.getState());
  }

  */
}