package edu.kit.satviz.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class ClientConnectionManager extends AbstractConnectionManager {

  private final ConnectionId cid;
  private final ConnectionContext ctx;

  public ClientConnectionManager(String address, int port, NetworkBlueprint bp) {
    super(bp);
    this.cid = new ConnectionId(new InetSocketAddress(address, port));
    this.ctx = new ConnectionContext(
        cid,
        new Receiver(bp::getBuilder),
        null
    );
  }

  @Override
  protected void processTerminateGlobal(boolean abnormal, String reason) {
    System.out.println("Client: processTerminateGlobal");
    // nothing to do
  }

  @Override
  protected void processStart() {
    System.out.println("Client: processStart");
    synchronized (syncState) {
      while (state == State.STARTED) {
        System.out.println("Client: tryConnect");
        try {
          if (ctx.tryConnect()) {
            state = State.OPEN;
            break;
          }
        } catch (IOException e) {
          terminateGlobal(true, "error while trying to connect");
          return;
        }
        System.out.println("Client: tryConnect failed");
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

      if (state == State.OPEN) {
        callConnect(ctx.getCid());
      }
    }

    System.out.println("Client: tryConnect done");
  }

  @Override
  protected void processSelectAcceptable(SelectionKey key) {
    // nothing to do
  }
}
