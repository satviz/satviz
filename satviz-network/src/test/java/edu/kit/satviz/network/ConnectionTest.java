package edu.kit.satviz.network;

import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionTest {
  private static final int PORT = 35214;

  private static NetworkBlueprint bp;
  private static ByteBuffer data;

  private static final Map<ConnectionId, List<NetworkMessage>> received = new HashMap<>();
  private static final List<NetworkMessage> receivedClient = new ArrayList<>();
  private static final List<ConnectionId> accepted = new ArrayList<>();
  private static final List<ConnectionId> connected = new ArrayList<>();
  private static String failListenedClient;
  private static String failListenedServer;
  private static final Object syncNewConnections = new Object();

  @BeforeAll
  static void initAll() {
    Map<Byte, Serializer<?>> m = new HashMap<>();
    m.put((byte) 1, new NullSerializer());
    m.put((byte) 2, new NullSerializer());
    bp = new NetworkBlueprint(m);
  }

  @BeforeEach
  void init() {
    received.clear();
    receivedClient.clear();
    accepted.clear();
    connected.clear();
    failListenedClient = null;
    failListenedServer = null;
  }

  @Test
  void testConnection() throws InterruptedException {
    ConnectionManager server = new ConnectionManager(PORT, bp);
    ClientConnectionManager client = new ClientConnectionManager("localhost", PORT, bp);

    System.out.println("initialized");

    server.registerConnect(ConnectionTest::defaultListenerAccept);
    server.registerGlobalFail(ConnectionTest::defaultListenerFailServer);
    client.registerConnect(ConnectionTest::defaultListenerConnect);
    client.registerGlobalFail(ConnectionTest::defaultListenerFailClient);

    System.out.println("registered");

    assertEquals(0, received.size());
    assertEquals(0, accepted.size());
    assertEquals(0, connected.size());
    assertNull(failListenedClient);
    assertNull(failListenedServer);

    try {
      assertTrue(client.start());
      assertFalse(client.start());

      System.out.println("client started");

      assertTrue(server.start());
      assertFalse(server.start());

      System.out.println("server started");

      synchronized (syncNewConnections) {
        while (connected.isEmpty()) {
          syncNewConnections.wait();
        }
      }

      System.out.println("connected");
      try {
        System.out.println("connected: " + connected.get(0).address());
        System.out.println("accepted: " + accepted.get(0).address());
      } catch (IndexOutOfBoundsException e) {
        fail("no connections received");
      }

      Thread.sleep(4000);
    } finally {
      System.out.println("cleanup client");
      client.stop();
      System.out.println("cleanup server");
      server.stop();
      System.out.println("cleanup done");
    }
  }

  static void defaultListener(ConnectionId cid, NetworkMessage msg) {
    List<NetworkMessage> l = received.computeIfAbsent(cid, k -> new ArrayList<>());
    l.add(msg);
  }

  static void defaultListenerAccept(ConnectionId newCid) {
    synchronized (syncNewConnections) {
      accepted.add(newCid);
      if (connected.size() == accepted.size()) {
        syncNewConnections.notifyAll();
      }
    }
  }

  static void defaultListenerConnect(ConnectionId newCid) {
    synchronized (syncNewConnections) {
      connected.add(newCid);
      if (connected.size() == accepted.size()) {
        syncNewConnections.notifyAll();
      }
    }
  }

  static void defaultListenerFailClient(String reason) {
    failListenedClient = reason;
  }

  static void defaultListenerFailServer(String reason) {
    failListenedServer = reason;
  }
}
