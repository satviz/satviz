package edu.kit.satviz.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class ClientConnectionManager extends AbstractConnectionManager {

  private final ConnectionContext ctx;
  private Selector select = null;

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
    System.out.println("Client: processTerminateGlobal");
    if (select != null) {
      try {
        select.close();
      } catch (IOException e) {
        // not our problem
      }
    }
    ctx.close(abnormal);
  }

  @Override
  protected void processStart() {
    System.out.println("Client: processStart");
    synchronized (syncState) {
      try {
        select = Selector.open();
      } catch (IOException e) {
        terminateGlobal(true, "error creating selector");
      }
      while (state == State.STARTED) {
        System.out.println("Client: tryConnect");
        try {
          if (ctx.tryConnect()) {
            state = State.OPEN;
            ctx.register(select, SelectionKey.OP_READ);
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

      if (state == State.OPEN) {
        callConnect(ctx.getCid());
      }
    }

    System.out.println("Client: tryConnect done");

    while (state == State.OPEN) {
      System.out.println("Client: processStart: read");
      try {
        select.select(1000);
      } catch (IOException e) {
        System.out.println("Client: selection error");
        terminateGlobal(true, "error selecting socket events");
        return;
      }
      Set<SelectionKey> keys = select.selectedKeys();
      if (!keys.isEmpty()) {
        // got an event; know it has to be our only socket
        keys.clear();
        doRead(ctx);
      }
    }
    // fall through as soon as state is State.FINISHING
    // clean up and terminate

    System.out.println("Client: processStart: done with state == OPEN; closing");
    terminateGlobal(false, "finished");
    synchronized (syncState) {
      syncState.notifyAll(); // thread is done
    }
    System.out.println("Client: processStart: finished");
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
