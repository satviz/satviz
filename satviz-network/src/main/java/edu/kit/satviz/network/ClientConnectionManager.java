package edu.kit.satviz.network;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ClientConnectionManager extends AbstractConnectionManager {

  private final ConnectionContext ctx;

  public ClientConnectionManager(String address, int port, NetworkBlueprint bp) {
    super(bp);
    this.ctx = new ConnectionContext(
        new ConnectionId(new InetSocketAddress(address, port)),
        new Receiver(bp::getBuilder),
        null
    );
  }

  @Override
  protected void processTerminateGlobal(boolean abnormal, String reason) {
    ctx.close(abnormal);
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

      if (state == State.OPEN) {
        callConnect(ctx.getCid());
      }
    }

    while (state == State.OPEN) {
      if (!doRead(ctx)) {
        terminateGlobal(true, "error while reading");
        return;
      }
    }
    // fall through as soon as state is State.FINISHING
    // clean up and terminate

    terminateGlobal(false, "finished");
    synchronized (syncState) {
      syncState.notifyAll(); // thread is done
    }
  }

  @Override
  protected void processStopNew() {
    ctx.close(false);
  }

  @Override
  protected ConnectionContext getContextFrom(ConnectionId cid) {
    return ctx.getCid() == cid ? ctx : null;
  }
}
