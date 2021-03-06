package edu.kit.satviz.network.general;

import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
 * IDs are passed out sequentially, starting at 0. No ID is ever reused for another connection.
 */
public class ConnectionServer implements AutoCloseable {

  private final NetworkBlueprint bp;
  private int numConnections = 0;
  private final List<Connection> connections = new CopyOnWriteArrayList<>();
  private final Selector sel;
  private final ServerSocketChannel serverChan;

  private Iterator<SelectionKey> selectedEvents = null;
  private int currentReadId;
  private Queue<NetworkMessage> currentRead;

  private final Object SYNC_READ = new Object();
  private final Object SYNC_CONNECTIONS = new Object();

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
    this.serverChan.bind(new InetSocketAddress(port));
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
    // this is still needed even though we have a concurrent list
    synchronized (SYNC_CONNECTIONS) {
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

  /**
   * Returns the next of the previously polled events, without polling for new ones.
   * @return pending event, possibly {@code null}
   */
  public PollEvent pollPrevious() {
    do {
      if (currentRead != null && !currentRead.isEmpty()) {
        return new PollEvent(PollEvent.EventType.READ, currentReadId, currentRead.poll());
      }
      if (selectedEvents == null || !selectedEvents.hasNext()) {
        break;
      }

      SelectionKey key = selectedEvents.next();
      selectedEvents.remove(); // avoid processing the same key twice

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
   * If there are no events pending, waits for at most 1000 ms for new events occurring.
   * This method is thread-safe; concurrent calls will always block until the pending poll
   *     operation is complete.
   * This method does not throw any exceptions, but rather returns them as events. This way, the
   *     caller can find out which connection produced the exception.
   * @return an event, possibly {@code null}
   */
  public PollEvent poll() {
    synchronized (SYNC_READ) {
      PollEvent event = pollPrevious();
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

      return pollPrevious();
    }
  }

  /**
   * Writes a {@link NetworkMessage} to one of the registered connections.
   * Concurrent writes to different connections are possible. Writes to the same connection are
   *     always synchronized.
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
   * Returns the remove address of one of the registered connections.
   * @param id the connection ID
   * @return the address of the connection
   * @throws IndexOutOfBoundsException if the ID is invalid
   * @throws IOException if an I/O error occurs
   */
  public InetSocketAddress getRemoteAddress(int id) throws IOException {
    Connection conn = connections.get(id);
    return conn.getRemoteAddress();
  }

  private void close(int id) {
    try {
      Connection conn = connections.get(id);
      conn.close();
    } catch (Exception e) {
      // do nothing if no connection was found or if some exception is thrown otherwise
    }
  }

  /**
   * Closes all registered connections, and the server socket itself.
   * Calling this method may cause concurrent polls or writes to fail.
   */
  public void close() {
    // synchronized to avoid new connections being made while we shut down
    synchronized (SYNC_CONNECTIONS) {
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
