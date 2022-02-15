package edu.kit.satviz.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;

/**
 * A client manager, connected to a {@link ServerConnectionManager}.
 */
public class ClientConnectionManager extends AbstractConnectionManager {

  private final ConnectionContext ctx;

  /**
   * Creates a new client manager without connecting to the server.
   *
   * @param address the address of the server
   * @param port the server port
   * @param bp the network blueprint
   */
  public ClientConnectionManager(String address, int port, NetworkBlueprint bp) {
    super(bp);
    ConnectionId cid = new ConnectionId(new InetSocketAddress(address, port));
    this.ctx = new ConnectionContext(
        cid,
        new Receiver(bp::getBuilder),
        null
    );
  }

  @Override
  protected void processTerminateGlobal(boolean abnormal, String reason) {
    // nothing to do
  }

  @Override
  protected void processStart() {
    synchronized (syncState) {
      while (state == State.STARTED) {
        try {
          if (ctx.tryConnect()) {
            state = State.OPEN;
            break;
          }
        } catch (IOException e) {
          terminateGlobal(true, "error while trying to connect");
          return;
        }
        try {
          syncState.wait(1000);
          // state might change in the meantime
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          terminateGlobal(true, "thread interrupted");
          return;
        }
      }
      if (!addContext(ctx)) {
        terminateGlobal(true, "could not create single client context");
      }
    }
  }

  @Override
  protected void processSelectAcceptable(SelectionKey key) {
    // nothing to do
  }
}
