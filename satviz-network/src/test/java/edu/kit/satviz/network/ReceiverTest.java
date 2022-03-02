package edu.kit.satviz.network;

import edu.kit.satviz.network.general.NetworkBlueprint;
import edu.kit.satviz.network.general.NetworkMessage;
import edu.kit.satviz.network.general.Receiver;
import edu.kit.satviz.serial.IntSerializer;
import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReceiverTest {

  private static final byte INT_MSG_NUM = 3;
  private static NetworkBlueprint bp;
  private static final IntSerializer serial = new IntSerializer();
  private Receiver r;

  @BeforeAll
  static void initAll() {
    Map<Byte, Serializer<?>> m = new HashMap<>();
    m.put(INT_MSG_NUM, serial);
    bp = new NetworkBlueprint(m);
  }

  @BeforeEach
  void initEach() {
    r = new Receiver(bp::getBuilder);
  }

  @Test
  void testReadMultipleAtOnce() throws IOException {
    // receive data from three different integers.
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

    byteOut.write(INT_MSG_NUM);
    serial.serialize(5, byteOut);
    byteOut.write(INT_MSG_NUM);
    serial.serialize(42, byteOut);
    byteOut.write(INT_MSG_NUM);
    serial.serialize(30000, byteOut);
    ByteBuffer bb = ByteBuffer.wrap(byteOut.toByteArray());

    NetworkMessage msg;

    msg = r.receive(bb);
    assertEquals(10, bb.remaining());
    assertNotNull(msg);
    assertEquals(NetworkMessage.State.PRESENT, msg.getState());
    assertEquals(INT_MSG_NUM, msg.getType());
    assertEquals(5, (Integer) msg.getObject());

    msg = r.receive(bb);
    assertEquals(5, bb.remaining());
    assertNotNull(msg);
    assertEquals(NetworkMessage.State.PRESENT, msg.getState());
    assertEquals(INT_MSG_NUM, msg.getType());
    assertEquals(42, (Integer) msg.getObject());

    msg = r.receive(bb);
    assertEquals(0, bb.remaining());
    assertNotNull(msg);
    assertEquals(NetworkMessage.State.PRESENT, msg.getState());
    assertEquals(INT_MSG_NUM, msg.getType());
    assertEquals(30000, (Integer) msg.getObject());

    assertNull(r.receive(bb));
  }

  @Test
  void testInterrupted() throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

    byteOut.write(INT_MSG_NUM);
    serial.serialize(123456789, byteOut);
    ByteBuffer bb = ByteBuffer.wrap(byteOut.toByteArray());

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
    bb.put((byte) (INT_MSG_NUM + 1));
    bb.flip();

    NetworkMessage msg = r.receive(bb);
    assertNotNull(msg);
    assertEquals(NetworkMessage.State.FAIL, msg.getState());
  }
}