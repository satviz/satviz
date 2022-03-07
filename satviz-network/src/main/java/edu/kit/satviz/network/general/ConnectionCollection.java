package edu.kit.satviz.network.general;

import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ConnectionCollection {

  public record PollEvent(EventType type, int id, Object obj) {
    public enum EventType {
      ACCEPT,
      READ,
      FAIL
    }
  }

  private final NetworkBlueprint bp;
  private int numConnections = 0;
  // use copy-on-write array list because we do way more accessing than inserting
  private final List<Connection> connections = new CopyOnWriteArrayList<>();
  private final Selector sel;
  private final ServerSocketChannel serverChan;

  private Iterator<SelectionKey> selectedEvents = null;
  private int currentReadId;
  private Queue<NetworkMessage> currentRead;


  public ConnectionCollection(NetworkBlueprint bp, int port) throws IOException {
    this.bp = bp;
    this.sel = Selector.open();
    this.serverChan = ServerSocketChannel.open();
    this.serverChan.configureBlocking(false);
    this.serverChan.register(this.sel, SelectionKey.OP_ACCEPT);
    // bind to wildcard IP address, and to fixed or automatically allocated port
    this.serverChan.bind(port == 0 ? null : new InetSocketAddress(port));
  }

  public InetSocketAddress getLocalAddress() throws IOException {
    return (InetSocketAddress) serverChan.getLocalAddress();
  }

  private PollEvent accept() {
    try {
      SocketChannel client = serverChan.accept();
      client.configureBlocking(false);
      // attach connection ID for quick find
      client.register(sel, SelectionKey.OP_READ, numConnections);
      connections.add(new Connection(client, bp));
    } catch (IOException e) {
      return new PollEvent(PollEvent.EventType.FAIL, -1, e);
    }
    return new PollEvent(PollEvent.EventType.ACCEPT, numConnections++, null);
  }

  private PollEvent processPending() {
    do {
      if (currentRead != null && !currentRead.isEmpty()) {
        return new PollEvent(PollEvent.EventType.READ, currentReadId, currentRead.poll());
      }
      if (!selectedEvents.hasNext()) {
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
        } catch (IOException | SerializationException e) {
          return new PollEvent(PollEvent.EventType.FAIL, currentReadId, e);
        }
      }
    } while (true);

    return null;
  }

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

  public void write(int id, byte type, Object obj) throws IOException, SerializationException {
    Connection conn = connections.get(id);
    conn.write(type, obj);
  }

  public void close(int id) throws IOException {
    Connection conn = connections.get(id);
    conn.close();
  }
}
