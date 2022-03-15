package edu.kit.satviz.network;

import edu.kit.satviz.network.general.Connection;
import edu.kit.satviz.network.general.ConnectionServer;
import edu.kit.satviz.network.general.NetworkMessage;
import edu.kit.satviz.network.general.PollEvent;
import edu.kit.satviz.network.pub.MessageTypes;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.serial.SerializationException;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

// TODO for some reason, using the same port on more than one test does not work, even if the
// TODO previous server socket was properly closed

class ConnectionTest {
  private static Connection client;
  public static ConnectionServer server;

  @Test
  void testLocalhost() {
    final int PORT = 34312;
    try {
      server = new ConnectionServer(PORT, MessageTypes.satvizBlueprint);
      client = new Connection("localhost", PORT, MessageTypes.satvizBlueprint);

      PollEvent event = null;
      while (event == null) {
        event = server.poll();
      }
      // client is connected with ID 0
      assertEquals(PollEvent.EventType.ACCEPT, event.type());
      assertEquals(0, event.id());
      assertNull(event.obj());

      // send two clauses in quick succession
      // note: this implicitly assumes that the server's receive buffer can hold both clauses
      Clause c1 = new Clause(new int[]{1,2,3,4,5,-1,-2,-3,-4,400000});
      Clause c2 = new Clause(new int[]{42});
      client.write(MessageTypes.CLAUSE_ADD, c1);
      client.write(MessageTypes.CLAUSE_DEL, c2);

      event = null;
      while (event == null) {
        event = server.poll();
      }
      assertEquals(PollEvent.EventType.READ, event.type());
      assertEquals(0, event.id());
      assertEquals(MessageTypes.CLAUSE_ADD, ((NetworkMessage) event.obj()).type());
      assertEquals(c1, ((NetworkMessage) event.obj()).object());
      event = null;
      while (event == null) {
        event = server.poll();
      }
      assertEquals(PollEvent.EventType.READ, event.type());
      assertEquals(0, event.id());
      assertEquals(MessageTypes.CLAUSE_DEL, ((NetworkMessage) event.obj()).type());
      assertEquals(c2, ((NetworkMessage) event.obj()).object());

      // make sure nothing else comes through
      event = server.poll();
      assertNull(event);

      // test writing in other direction
      Queue<NetworkMessage> q;
      q = client.read();
      assertTrue(q.isEmpty());
      server.write(0, MessageTypes.CLAUSE_ADD, c1);
      do {
        q = client.read();
      } while (q.isEmpty());
      assertEquals(q.size(), 1);
      assertEquals(MessageTypes.CLAUSE_ADD, q.peek().type());
      assertEquals(c1, q.peek().object());

    } catch (Throwable t) {
      fail(t);
    } finally {
      if (client != null) client.close();
      client = null;
      if (server != null) server.close();
      server = null;
    }
  }

  @Test
  void testFail() {
    final int PORT = 34313;
    try {
      server = new ConnectionServer(PORT, MessageTypes.satvizBlueprint);
      client = new Connection("localhost", PORT, MessageTypes.satvizBlueprint);

      assertThrows(SerializationException.class, () -> {
        client.write(MessageTypes.CLAUSE_ADD, client); // wrong type
      });
      assertThrows(SerializationException.class, () -> {
        client.write(MessageTypes.CLAUSE_ADD, new Clause(new int[]{1,2,3})); // correct now
      });

      assertThrows(IndexOutOfBoundsException.class, () -> {
        server.write(42, MessageTypes.START, null); // invalid connection ID on server
      });

    } catch (Throwable t) {
      fail(t);
    } finally {
      if (client != null) client.close();
      client = null;
      if (server != null) server.close();
      server = null;
    }
  }

  @Test
  void testBindEphemeral() {
    final int PORT = 0;
    try {
      server = new ConnectionServer(PORT, MessageTypes.satvizBlueprint);
      // check to see if we got a sensible port
      assertTrue(server.getLocalAddress().getPort() >= 1024);
      assertTrue(server.getLocalAddress().getPort() <= 65535);
    } catch (Throwable t) {
      fail(t);
    } finally {
      if (server != null) server.close();
      server = null;
    }
  }
}
