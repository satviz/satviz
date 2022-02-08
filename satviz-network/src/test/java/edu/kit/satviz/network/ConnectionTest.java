package edu.kit.satviz.network;

import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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

  private static final Map<ConnectionId, List<NetworkMessage>> receivedServer = new HashMap<>();
  private static final List<NetworkMessage> receivedClient = new ArrayList<>();
  private static final List<ConnectionId> accepted = new ArrayList<>();
  private static final List<ConnectionId> connected = new ArrayList<>();
  private static String failListenedClient;
  private static String failListenedServer;
  private static final Object syncNewConnections = new Object();

  private static ConnectionManager server;
  private static ClientConnectionManager client;

  @BeforeAll
  static void initAll() {
    Map<Byte, Serializer<?>> m = new HashMap<>();
    m.put((byte) 1, new NullSerializer());
    m.put((byte) 2, new NullSerializer());
    bp = new NetworkBlueprint(m);
  }

  @BeforeEach
  void init() {
    server = null;
    client = null;
    receivedServer.clear();
    receivedClient.clear();
    accepted.clear();
    connected.clear();
    failListenedClient = null;
    failListenedServer = null;
  }

  @Test
  void testConnection() throws InterruptedException {
    server = new ConnectionManager(PORT, bp);
    client = new ClientConnectionManager("localhost", PORT, bp);

    System.out.println("initialized");

    server.registerConnect(ConnectionTest::defaultListenerAccept);
    server.registerGlobalFail(ConnectionTest::defaultListenerFailServer);
    client.registerConnect(ConnectionTest::defaultListenerConnect);
    client.registerGlobalFail(ConnectionTest::defaultListenerFailClient);

    System.out.println("registered");

    assertEquals(0, receivedServer.size());
    assertEquals(0, receivedClient.size());
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

      Thread.sleep(5000);
      /*
      try {
        client.send(connected.get(0), (byte) 2, null);
      } catch (IOException e) {
        fail("client send did not work:" + e);
      }
      synchronized (receivedServer) {
        while (receivedServer.get(accepted.get(0)) == null || receivedServer.get(accepted.get(0)).isEmpty()) {
          receivedServer.wait();
        }
      }
      List<NetworkMessage> l = receivedServer.get(accepted.get(0));
      assertEquals(1, l.size());
      NetworkMessage msg = l.get(0);
      assertNotNull(msg);
      assertEquals(NetworkMessage.State.PRESENT, msg.getState());
      assertEquals(2, msg.getType());
       */
    } finally {
      System.out.println("cleanup client");
      client.stop();
      System.out.println("cleanup server");
      server.stop();
      System.out.println("cleanup done");
    }
  }

  static void defaultListener(ConnectionId cid, NetworkMessage msg) {
    synchronized (receivedServer) {
      System.out.println("received");
      List<NetworkMessage> l = receivedServer.computeIfAbsent(cid, k -> new ArrayList<>());
      l.add(msg);
    }
  }

  static void defaultListenerAccept(ConnectionId newCid) {
    server.register(newCid, ConnectionTest::defaultListener);
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
