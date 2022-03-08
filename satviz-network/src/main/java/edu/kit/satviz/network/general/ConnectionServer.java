package edu.kit.satviz.network.general;

import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A server connection to send and receive {@link NetworkMessage}s.
 * The server may hold an arbitrary amount of {@link Connection}s.
 * Accepting new connections and reading from active connections is done asynchronously, while
 *     writing to connections is synchronous.
 * The active connections are identified by an ID, which is a small, non-negative integer.
 */
public class ConnectionServer implements AutoCloseable {

  /**
   * An event on a {@link ConnectionServer}.
   * @param type the event type
   * @param id the id of the connection that this event concerns, -1 if global
   * @param obj the object of the event
   */
  public record PollEvent(EventType type, int id, Object obj) {
    /** The type of the event. */
    public enum EventType {
      /** A new connection is accepted. <code>obj</code> is <code>null</code>. */
      ACCEPT,
      /** A message was read from a connection. <code>obj</code> is a {@link NetworkMessage} */
      READ,
      /** A connection (or the entire server) failed. <code>obj</code> is an {@link Exception} */
      FAIL
    }
  }

  private final NetworkBlueprint bp;
  private int numConnections = 0;
  private final List<Connection> connections = new CopyOnWriteArrayList<>();
  private final Selector sel;
  private final ServerSocketChannel serverChan;

  private Iterator<SelectionKey> selectedEvents = null;
  private int currentReadId;
  private Queue<NetworkMessage> currentRead;

  private final Object SYNC = new Object();

  /**
   * Creates a new connection server by opening a server socket channel.
   * @param bp the types of messages
   * @param port the server port, 0 for automatic assignment
   * @throws IOException if an I/O error occurs
   */
  public ConnectionServer(int port, NetworkBlueprint bp) throws IOException {
    this.bp = bp;
    this.sel = Selector.open();
    this.serverChan = ServerSocketChannel.open();
    this.serverChan.configureBlocking(false);
    this.serverChan.register(this.sel, SelectionKey.OP_ACCEPT);
    // bind to wildcard IP address
    this.serverChan.bind(port == 0 ? null : new InetSocketAddress(port));
  }

  /**
   * Returns the local address that this server is bound to.
   * @return local address
   * @throws ClosedChannelException if the channel is closed
   * @throws IOException if an I/O error occurs
   */
  public InetSocketAddress getLocalAddress() throws IOException {
    return (InetSocketAddress) serverChan.getLocalAddress();
  }

  private PollEvent accept() {
    // synchronized to avoid new connections being made while we shut down
    synchronized (SYNC) {
      try {
        SocketChannel client = serverChan.accept();
        client.configureBlocking(false);
        // attach connection ID for quick find
        client.register(sel, SelectionKey.OP_READ, numConnections);
        connections.add(new Connection(client, bp));
      } catch (Exception e) {
        return new PollEvent(PollEvent.EventType.FAIL, -1, e);
      }
      return new PollEvent(PollEvent.EventType.ACCEPT, numConnections++, null);
    }
  }

  private PollEvent processPending() {
    do {
      if (currentRead != null && !currentRead.isEmpty()) {
        return new PollEvent(PollEvent.EventType.READ, currentReadId, currentRead.poll());
      }
      if (selectedEvents == null || !selectedEvents.hasNext()) {
        break;
      }

      SelectionKey key = selectedEvents.next();
      selectedEvents.remove();

      if (key.isAcceptable()) {
        return accept();
      } else if (key.isReadable()) {
        currentReadId = (int) key.attachment();
        Connection conn = connections.get(currentReadId);
        try {
          currentRead = conn.read();
        } catch (Exception e) {
          currentRead = null;
          return new PollEvent(PollEvent.EventType.FAIL, currentReadId, e);
        }
      }
    } while (true); // finite loop because iterator has finite length

    return null;
  }

  /**
   * Polls for a single event on the server socket or on any of the registered connections.
   * If there are no events pending, waits for at most 1000 s for new events occurring.
   * This method does not throw any exceptions, but rather returns them as events.
   * This way, the caller can find out which connection produced the exception.
   * @return an event, possibly <code>null</code>
   */
  public PollEvent poll() {
    PollEvent event = processPending();
    if (event != null) {
      return event;
    }

    // found no events remaining; poll new
    try {
      sel.select(1000);
    } catch (Exception e) {
      return new PollEvent(PollEvent.EventType.FAIL, -1, e);
    }
    selectedEvents = sel.selectedKeys().iterator();

    return processPending();
  }

  /**
   * Writes a {@link NetworkMessage} to one of the registered connections.
   * @param id the connection ID
   * @param type the message type
   * @param obj the message object
   * @throws IndexOutOfBoundsException if the ID is invalid
   * @throws IOException if an I/O error occurs
   * @throws SerializationException if the message cannot be encoded for this connection
   */
  public void write(int id, byte type, Object obj) throws IOException, SerializationException {
    Connection conn = connections.get(id);
    conn.write(type, obj);
  }

  /**
   * Closes one of the registered connections.
   * If the passed ID is not associated with a connection, nothing happens.
   * @param id the connection ID
   */
  public void close(int id) {
    try {
      Connection conn = connections.get(id);
      conn.close();
    } catch (Exception e) {
      // do nothing if no connection was found or if some exception is thrown otherwise
    }
  }

  /**
   * Closes all registered connections, and the server socket itself.
   */
  public void close() {
    // synchronized to avoid new connections being made while we shut down
    synchronized (SYNC) {
      for (int i = 0; i < numConnections; i++) {
        close(i);
      }
      try {
        serverChan.close();
      } catch (Exception e) {
        // do nothing more; don't propagate exceptions to the outside
        // shutdown should not throw exceptions
      }
    }
  }
}
