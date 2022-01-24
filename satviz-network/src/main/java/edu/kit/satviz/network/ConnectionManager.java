package edu.kit.satviz.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ConnectionManager {
  private enum State {
    NEW,
    STARTED,
    OPEN,
    FAILED,
    FINISHED
  }

  private final Object syncState = new Object();

  private volatile State state; // TODO really volatile?
  private ServerSocketChannel serverChan;
  private Selector select;
  private final InetSocketAddress serverAddress;
  private final List<ConnectionContext> contexts;
  private final NetworkBlueprint bp;

  public ConnectionManager(int port, NetworkBlueprint bp) {
    serverAddress = new InetSocketAddress("localhost", port);
    this.contexts = new ArrayList<>();
    this.bp = bp;
  }

  private void terminateGlobal(boolean abnormal) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return;
      }

      if (state == State.OPEN) {
        try {
          serverChan.close();
          select.close();
        } catch (IOException e) {
          // not our problem
        }
        for (ConnectionContext ctx : contexts) {
          ctx.close(abnormal);
        }
      }
      state = abnormal ? State.FAILED : State.FINISHED;
    }
  }

  private void doStart() {
    synchronized (syncState) {
      try {
        serverChan = ServerSocketChannel.open();
        serverChan.bind(serverAddress);
        serverChan.configureBlocking(false);
        select = Selector.open();
        serverChan.register(select, SelectionKey.OP_ACCEPT); // TODO do we need this key?
      } catch (IOException e) {
        terminateGlobal(true);
        return;
      }
      state = State.OPEN;
    }

    while (state == State.OPEN) {
      try {
        int numEvents = select.select();
      } catch (IOException e) {
        terminateGlobal(true);
        return;
      }
      Iterator<SelectionKey> iter = select.selectedKeys().iterator();
      while (iter.hasNext()) {
        SelectionKey key = iter.next();
        if (key.isAcceptable()) {
          SocketChannel newChan = serverChan.accept();
          // TODO create new ConnectionContext (with existing chan)
        }
        iter.remove();
      }
    }
  }

  public boolean start() {
    synchronized (syncState) {
      if (state != State.NEW) {
        return false;
      }
      state = State.STARTED;
    }

    new Thread(this::doStart).start();
    return true;
  }
}
