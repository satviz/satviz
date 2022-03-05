package edu.kit.satviz.network.general;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * A server manager, servicing multiple {@link ClientConnectionManager}s.
 */
public class ServerConnectionManager extends AbstractConnectionManager {

  private final int port;
  private ServerSocketChannel serverChan = null;

  /**
   * Creates a server manager.
   * It is not bound to the port yet. Call <code>start()</code> to initiate this.
   *
   * @param port the port, 0 to assign automatically
   * @param bp the network blueprint
   */
  public ServerConnectionManager(int port, NetworkBlueprint bp) {
    super(bp);
    this.port = port;
  }

  @Override
  protected void processTerminateGlobal(boolean abnormal, String reason) {
    if (serverChan != null) {
      try {
        serverChan.close();
      } catch (IOException e) {
        // not our problem
      }
    }
  }

  @Override
  protected void processSelectAcceptable(SelectionKey key) {
    SocketChannel newChan;
    try {
      newChan = serverChan.accept();
    } catch (IOException e) {
      // we take this as a fatal condition
      terminateGlobal(true, "error accepting new connection");
      return;
    }
    if (newChan == null) { // only call this function with acceptable event
      terminateGlobal(true, "error accepting new connection");
      return;
    }
    addContext(newChan);
  }

  @Override
  protected void processStart() {
    synchronized (syncState) {
      if (state != State.STARTED) { // someone already terminated
        return;
      }
      try {
        serverChan = ServerSocketChannel.open();
        serverChan.configureBlocking(false);
        addAcceptable(serverChan);
        serverChan.bind(port != 0 ? new InetSocketAddress(port) : null);
      } catch (IOException e) {
        terminateGlobal(true, "unable to create server socket");
        return;
      }
      state = State.OPEN;
    }
  }

  /**
   * Gets the local address that the server socket is bound to.
   *
   * @return local address, null if not bound
   */
  public InetSocketAddress getLocalAddress() {
    if (serverChan != null) {
      try {
        return (InetSocketAddress) serverChan.getLocalAddress();
      } catch (IOException e) {
        return null;
      }
    }
    return null;
  }
}
