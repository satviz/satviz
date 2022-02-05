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
    System.out.println("Server: processTerminateGlobal");
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
    System.out.println("Server: processTerminateGlobal: close contexts");
    for (ConnectionContext ctx : contexts) {
      // TODO assure that no new context is added during this close
      ctx.close(abnormal);
    }
    System.out.println("Server: processTerminateGlobal: close contexts done");
  }

  private void open() {
    System.out.println("Server: open");
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
    System.out.println("Server: open successful");
  }

  private boolean acceptNew() {
    System.out.println("Server: acceptNew");
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
    System.out.println("Server: acceptNew successful");
    return true;
  }

  private void pollAll() {
    System.out.println("Server: pollAll");
    synchronized (syncState) {
      System.out.println("Server: pollAll: got lock");
      if (state != State.OPEN) {
        System.out.println("Server: pollAll: state not open before selecting");
        return;
      }

      try {
        select.select(1000);
      } catch (IOException e) {
        System.out.println("Server: selection error");
        terminateGlobal(true, "error selecting socket events");
        return;
      }
    }
    System.out.println("Server: pollAll: selected");
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
        System.out.println("Server: pollAll: read event");
        doRead(getContextFrom((SocketChannel) key.channel()));
      }
      iter.remove();
    }
    System.out.println("Server: pollAll successful");
  }

  @Override
  protected void processStart() {
    System.out.println("Server: processStart");
    open();

    while (state == State.OPEN) {
      pollAll();
    }
    System.out.println("Server: processStart: done with state == State.OPEN; closing");

    terminateGlobal(false, "finished");
    synchronized (syncState) {
      syncState.notifyAll(); // thread is done
    }
    System.out.println("Server: processStart done");
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
