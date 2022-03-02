package edu.kit.satviz.network;

import edu.kit.satviz.network.general.*;
import edu.kit.satviz.serial.NullSerializer;
import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContextTest {
  private static final int PORT = 35124;
  private static final byte NULL_SERIALIZE_BYTE = 5;

  private static NetworkBlueprint bp;

  private static final List<NetworkMessage> received = new ArrayList<>();
  private static final ByteBuffer bb = ByteBuffer.allocate(1024);

  @BeforeAll
  static void initAll() {
    Map<Byte, Serializer<?>> m = new HashMap<>();
    m.put(NULL_SERIALIZE_BYTE, new NullSerializer());
    bp = new NetworkBlueprint(m);
  }

  @BeforeEach
  void init() {
    received.clear();
  }

  @Test
  void testNoneListening() throws IOException {
    ConnectionContext ctx = new ConnectionContext(
        new ConnectionId(new InetSocketAddress("localhost", PORT)),
        new Receiver(bp::getBuilder),
        ContextTest::defaultListener
    );
    assertFalse(ctx.tryConnect());
    assertEquals(0, received.size());

    assertThrows(IOException.class, () -> ctx.read(bb));
    assertThrows(IOException.class, () -> ctx.write(bb));

    ctx.close(false);

    assertThrows(IOException.class, () -> ctx.read(bb));
    assertThrows(IOException.class, () -> ctx.write(bb));

    assertEquals(1, received.size());
    assertEquals(NetworkMessage.State.TERM, received.get(0).getState());
  }

  static void defaultListener(ConnectionId cid, NetworkMessage msg) {
    received.add(msg);
  }

  @Test
  void testReadWrite() throws IOException {
    InetSocketAddress remote = new InetSocketAddress("localhost", PORT);
    ConnectionContext ctx = new ConnectionContext(
        new ConnectionId(remote),
        new Receiver(bp::getBuilder),
        ContextTest::defaultListener
    );

    ServerSocketChannel serverChan = null;
    SocketChannel chan = null;
    ByteBuffer message = ByteBuffer.allocate(2);
    try {
      assertFalse(ctx.tryConnect());

      serverChan = ServerSocketChannel.open();
      serverChan.bind(remote);

      assertTrue(ctx.tryConnect());
      chan = serverChan.accept();

      // write to context, read from other socket
      message.put(NULL_SERIALIZE_BYTE);
      message.put((byte) 0);
      message.flip();
      int numWritten = ctx.write(message);
      assertEquals(2, numWritten);

      message.clear();
      int numRead = chan.read(message);
      message.flip();
      assertEquals(2, numRead);
      assertEquals(NULL_SERIALIZE_BYTE, message.get());
      assertEquals(0, message.get());

      // write to other socket, read from context
      message.clear();
      message.put(NULL_SERIALIZE_BYTE);
      message.put((byte) 0);
      message.flip();
      numWritten = chan.write(message);
      assertEquals(2, numWritten);

      message.clear(); // for safety
      assertEquals(0, received.size());
      ctx.read(message);
      assertEquals(1, received.size());
      NetworkMessage msg = received.get(0);
      assertEquals(NetworkMessage.State.PRESENT, msg.getState());
      assertEquals(NULL_SERIALIZE_BYTE, msg.getType());

    } finally {
      if (serverChan != null) {
        serverChan.close();
      }
      if (chan != null) {
        chan.close();
      }
      ctx.close(false);
    }
  }
}
