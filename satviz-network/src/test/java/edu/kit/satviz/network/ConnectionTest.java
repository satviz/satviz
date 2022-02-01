package edu.kit.satviz.network;

import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionTest {
  private static final int PORT = 35214;

  private static NetworkBlueprint bp;
  private static ByteBuffer data;

  private static List<NetworkMessage> received;
  private static List<ConnectionId> accepted;
  private static boolean connectedListened;
  private static String failListened;

  @BeforeAll
  static void initAll() {
    Map<Byte, Serializer<?>> m = new HashMap<>();
    m.put((byte) 1, new NullSerializer());
    m.put((byte) 2, new NullSerializer());
    bp = new NetworkBlueprint(m);
    data = ByteBuffer.allocate(16);
  }

  @BeforeEach
  void init() {
    received = new ArrayList<>();
    accepted = new ArrayList<>();
    connectedListened = false;
    failListened = null;
  }

  @Test
  void testConnection() throws InterruptedException {
    ConnectionManager server = new ConnectionManager(PORT, bp);
    ClientConnectionManager client = new ClientConnectionManager("localhost", PORT, bp);

    System.out.println("initialized");

    server.registerAccept(ConnectionTest::defaultListenerAccept);
    server.registerGlobalFail(ConnectionTest::defaultListenerFail);
    client.registerConnect(ConnectionTest::defaultListenerConnect);
    client.registerGlobalFail(ConnectionTest::defaultListenerFail);

    System.out.println("registered");

    assertEquals(0, received.size());
    assertEquals(0, accepted.size());
    assertFalse(connectedListened);
    assertNull(failListened);

    try {
      assertTrue(client.start());
      assertFalse(client.start());

      System.out.println("client started");

      assertFalse(connectedListened);
      assertNull(failListened);

    } finally {
      client.stop();
      server.stop();
    }
  }

  static void defaultListener(ConnectionId cid, NetworkMessage msg) {
    received.add(msg);
  }

  static void defaultListenerAccept(ConnectionId newCid) {
    accepted.add(newCid);
  }

  static void defaultListenerConnect(ConnectionId newCid) {
    connectedListened = true;
  }

  static void defaultListenerFail(String reason) {
    failListened = reason;
  }
}
