package edu.kit.satviz.network;

import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContextTest {
  private static final int PORT = 35124;

  private static NetworkBlueprint bp;

  private static final List<NetworkMessage> received = new ArrayList<>();
  private static final ByteBuffer bb = ByteBuffer.allocate(1024);

  @BeforeAll
  static void initAll() {
    Map<Byte, Serializer<?>> m = new HashMap<>();
    m.put((byte) 1, new NullSerializer());
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

    assertThrows(NotYetConnectedException.class, () -> ctx.read(bb));
    assertThrows(NotYetConnectedException.class, () -> ctx.write(bb));

    ctx.close(false);

    assertThrows(NotYetConnectedException.class, () -> ctx.read(bb));
    assertThrows(NotYetConnectedException.class, () -> ctx.write(bb));

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

    assertFalse(ctx.tryConnect());

    ServerSocketChannel serverChan = ServerSocketChannel.open();
    serverChan.bind(remote);

    assertTrue(ctx.tryConnect());
    SocketChannel chan = serverChan.accept();

    ctx.close(false);

    assertThrows(AlreadyConnectedException.class, ctx::tryConnect);

    serverChan.close();
  }
}
