package edu.kit.satviz.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager extends AbstractConnectionManager {

  private final InetSocketAddress serverAddress;
  private ServerSocketChannel serverChan = null;
  private Selector select = null;

  private final Set<ConnectionContext> contexts;

  public ConnectionManager(int port, NetworkBlueprint bp) {
    super(bp);
    serverAddress = new InetSocketAddress("localhost", port);
    this.contexts = ConcurrentHashMap.newKeySet();
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
    if (select != null) {
      try {
        select.close();
      } catch (IOException e) {
        // not our problem
      }
    }
    for (ConnectionContext ctx : contexts) {
      // TODO assure that no new context is added during this close
      ctx.close(abnormal);
    }
  }

  private void open() {
    synchronized (syncState) {
      if (state != State.STARTED) { // someone already terminated
        return;
      }
      try {
        serverChan = ServerSocketChannel.open();
        serverChan.configureBlocking(false);
        serverChan.bind(serverAddress);
        select = Selector.open();
        serverChan.register(select, SelectionKey.OP_ACCEPT);
      } catch (IOException e) {
        terminateGlobal(true, "unable to create server socket");
        return;
      }
      state = State.OPEN;
    }
  }

  private boolean acceptNew() {
    synchronized (syncState) {
      if (state != State.OPEN) {
        return true;
      }

      ConnectionContext newCtx;
      try {
        SocketChannel newChan = serverChan.accept();
        if (newChan == null) { // only call this function with acceptable event
          terminateGlobal(true, "error accepting new connection");
          return false;
        }
        newChan.configureBlocking(false);
        newChan.register(select, SelectionKey.OP_READ);
        newCtx = new ConnectionContext(
            newChan,
            new Receiver(bp::getBuilder),
            null
        );
      } catch (IOException e) {
        // we take this as a fatal condition
        terminateGlobal(true, "error accepting new connection");
        return false;
      }
      contexts.add(newCtx);
      callConnect(newCtx.getCid());
    }
    return true;
  }

  private void pollAll() {
    synchronized (syncState) {
      if (state != State.OPEN) {
        return;
      }

      try {
        select.select();
      } catch (IOException e) {
        terminateGlobal(true, "error selecting socket events");
        return;
      }
    }

    Iterator<SelectionKey> iter = select.selectedKeys().iterator();
    while (iter.hasNext()) {
      SelectionKey key = iter.next();
      if (key.isAcceptable()) { // must be the server socket
        if (!acceptNew()) {
          // has terminated
          return;
        }
      }
      if (key.isReadable()) {
        doRead(getContextFrom((SocketChannel) key.channel()));
      }
      iter.remove();
    }
  }

  @Override
  protected void processStart() {
    open();

    while (state == State.OPEN) {
      pollAll();
    }

    terminateGlobal(false, "finished");
    synchronized (syncState) {
      syncState.notifyAll(); // thread is done
    }
  }

  @Override
  protected ConnectionContext getContextFrom(ConnectionId cid) {
    for (ConnectionContext ctx : contexts) {
      if (ctx.getCid() == cid) {
        return ctx;
      }
    }
    return null;
  }

  private ConnectionContext getContextFrom(SocketChannel chan) {
    for (ConnectionContext ctx : contexts) {
      if (ctx.usesChannel(chan)) {
        return ctx;
      }
    }
    return null;
  }

  @Override
  protected void processStopNew() {
    // nothing to do
  }
}
