package edu.kit.satviz.network;

import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ContextTest {

  private static NetworkBlueprint bp;
  private static List<NetworkMessage> received;
  private static ConnectionId cid;
  private static ByteBuffer data;

  @BeforeAll
  static void initAll() {
    Map<Byte, Serializer<?>> m = new HashMap<>();
    m.put((byte) 1, new NullSerializer());
    m.put((byte) 2, new NullSerializer());
    bp = new NetworkBlueprint(m);
    cid = new ConnectionId(new InetSocketAddress("localhost", 35214));
    data = ByteBuffer.allocate(16);
    data.put((byte) 0);
  }

  @BeforeEach
  void init() {
    received = new ArrayList<>();
  }

  @Test
  void testNoneListening() throws IOException {
    ConnectionContext ctx = new ConnectionContext(
        cid,
        new Receiver(bp::getBuilder),
        ContextTest::defaultListener
    );
    assertFalse(ctx.tryConnect());
    assertEquals(0, received.size());
    ctx.close(false);
    assertEquals(1, received.size());
    assertEquals(NetworkMessage.State.TERM, received.get(0).getState());
  }

  @Test
  void testClientConnection() throws IOException {
    ConnectionContext ctx = new ConnectionContext(
        cid,
        new Receiver(bp::getBuilder),
        ContextTest::defaultListener
    );
    ServerSocketChannel serverChan = null;
    try {
      serverChan = ServerSocketChannel.open();
      serverChan.bind(cid.address());
    } catch (IOException e) {
      if (serverChan != null) {
        serverChan.close();
      }
      throw e;
    }

    try {
      assertTrue(ctx.tryConnect());
    } catch (IOException e) {
      ctx.close(true);
      serverChan.close();
      throw e;
    }

    /*
    try {
      ctx.write(data);
    } catch (NotYetConnectedException | IOException e) {
      ctx.close(true);
      serverChan.close();
      throw new IOException(e);
    }
    */

    // TODO accept to read data

    ctx.close(false);
    serverChan.close();
  }

  static void defaultListener(ConnectionId cid, NetworkMessage msg) {
    received.add(msg);
  }
}
