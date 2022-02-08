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

  public ConnectionManager(int port, NetworkBlueprint bp) {
    super(bp);
    serverAddress = new InetSocketAddress("localhost", port);
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
    System.out.println("Server: processTerminateGlobal: close contexts done");
  }

  @Override
  protected void processSelectAcceptable(SelectionKey key) {
    System.out.println("Server: acceptNew");
    synchronized (syncState) {
      if (state != State.OPEN) {
        return;
      }

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
      ConnectionContext newCtx = addContext(newChan);
      if (newCtx != null) {
        callConnect(newCtx.getCid());
      }
    }
    System.out.println("Server: acceptNew successful");
  }

  @Override
  protected void processStart() {
    System.out.println("Server: open");
    synchronized (syncState) {
      if (state != State.STARTED) { // someone already terminated
        return;
      }
      try {
        serverChan = ServerSocketChannel.open();
        serverChan.configureBlocking(false);
        addToSelector(serverChan, SelectionKey.OP_ACCEPT);
        serverChan.bind(serverAddress);
      } catch (IOException e) {
        terminateGlobal(true, "unable to create server socket");
        return;
      }
      state = State.OPEN;
    }
    System.out.println("Server: open successful");
  }
}
