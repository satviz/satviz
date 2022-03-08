package edu.kit.satviz.network;

import edu.kit.satviz.network.general.Connection;
import edu.kit.satviz.network.general.ConnectionServer;
import edu.kit.satviz.network.general.NetworkMessage;
import edu.kit.satviz.network.pub.MessageTypes;
import edu.kit.satviz.sat.Clause;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionTest {
  private static Connection client;
  public static ConnectionServer server;
  public static final int PORT = 34312;

  @Test
  void testLocalhost() {
    System.out.println("starting testLocalhost");
    try {
      server = new ConnectionServer(PORT, MessageTypes.satvizBlueprint);
      client = new Connection("localhost", PORT, MessageTypes.satvizBlueprint);

      ConnectionServer.PollEvent event = null;
      while (event == null) {
        event = server.poll();
      }
      // client is connected with ID 0
      assertEquals(ConnectionServer.PollEvent.EventType.ACCEPT, event.type());
      assertEquals(0, event.id());
      assertNull(event.obj());

      Clause c1 = new Clause(new int[]{1,2,3,4,5,-1,-2,-3,-4,400000});
      Clause c2 = new Clause(new int[]{42});
      client.write(MessageTypes.CLAUSE_ADD, c1);
      client.write(MessageTypes.CLAUSE_DEL, c2);

      event = null;
      while (event == null) {
        event = server.poll();
      }
      assertEquals(ConnectionServer.PollEvent.EventType.READ, event.type());
      assertEquals(0, event.id());
      assertEquals(MessageTypes.CLAUSE_ADD, ((NetworkMessage) event.obj()).getType());
      assertEquals(c1, ((NetworkMessage) event.obj()).getObject());
      event = null;
      while (event == null) {
        event = server.poll();
      }
      assertEquals(ConnectionServer.PollEvent.EventType.READ, event.type());
      assertEquals(0, event.id());
      assertEquals(MessageTypes.CLAUSE_DEL, ((NetworkMessage) event.obj()).getType());
      assertEquals(c2, ((NetworkMessage) event.obj()).getObject());

    } catch (Throwable t) {
      fail(t);
    } finally {
      if (client != null) client.close();
      if (server != null) server.close();
    }
  }

  @Test
  void testTrue() {
    System.out.println("Sanity!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    assertEquals(1, 1);
  }
}
