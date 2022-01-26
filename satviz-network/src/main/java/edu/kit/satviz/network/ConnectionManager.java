package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ConnectionManager {
  private enum State {
    NEW,
    STARTED,
    OPEN,
    FINISHING,
    FINISHED,
    FAILED
  }

  private final Object syncState = new Object();
  private volatile State state = State.NEW;

  private final InetSocketAddress serverAddress;
  private ServerSocketChannel serverChan = null;
  private Selector select = null;

  private final Set<ConnectionContext> contexts;
  private final NetworkBlueprint bp;
  private final ByteBuffer readBuf = ByteBuffer.allocate(1024);

  private Consumer<ConnectionId> lsAccept;
  private Consumer<String> lsFail;

  public ConnectionManager(int port, NetworkBlueprint bp) {
    serverAddress = new InetSocketAddress("localhost", port);
    this.contexts = ConcurrentHashMap.newKeySet();
    this.bp = bp;
  }

  private void terminateGlobal(boolean abnormal) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return;
      }

      if (state == State.OPEN || state == State.FINISHING) {
        try {
          serverChan.close();
        } catch (IOException e) {
          // not our problem
        }
        try {
          select.close();
        } catch (IOException e) {
          // not our problem
        }
        for (ConnectionContext ctx : contexts) {
          ctx.close(abnormal);
        }
      }
      state = abnormal ? State.FAILED : State.FINISHED;
      if (state == State.FAILED) {
        lsFail.accept("fail");
      }
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
        terminateGlobal(true);
        return;
      }
      state = State.OPEN;
    }
  }

  public boolean isClosed() {
    return state == State.FINISHED || state == State.FAILED;
  }

  private boolean acceptNew() {
    ConnectionContext newCtx = null;
    try {
      SocketChannel newChan = serverChan.accept();
      if (newChan == null) { // only call this function with acceptable event
        terminateGlobal(true);
        return false;
      }
      newChan.configureBlocking(false);
      newChan.register(select, SelectionKey.OP_READ);
      newCtx = new ConnectionContext(
          new ConnectionId((InetSocketAddress) newChan.getRemoteAddress()),
          newChan,
          new Receiver(bp::getBuilder),
          null
      );
    } catch (IOException e) {
      // we take this as a fatal condition
      terminateGlobal(true);
      return false;
    }
    contexts.add(newCtx);
    if (lsAccept != null) {
      lsAccept.accept(newCtx.getCid());
    }
    return true;
  }

  private boolean doRead(SelectionKey key) {
    SocketChannel chan = (SocketChannel) key.channel();
    ConnectionContext ctx = getContextFrom(chan);
    if (ctx == null) {
      return false; // this is global fail if we don't remove things from the list
    }
    try {
      ctx.read(readBuf);
    } catch (IOException e) {
      ctx.close(true);
      return false;
    }
    return true;
  }

  private void pollAll() {
    try {
      select.select();
    } catch (IOException e) {
      terminateGlobal(true);
      return;
    }
    Iterator<SelectionKey> iter = select.selectedKeys().iterator();
    while (iter.hasNext()) {
      SelectionKey key = iter.next();
      if (key.isAcceptable()) { // must be the server socket
        if (!acceptNew()) {
          return;
        }
      }
      if (key.isReadable()) {
        doRead(key);
      }
      iter.remove();
    }
  }

  private void doStart() {
    open();

    while (state == State.OPEN) {
      pollAll();
    }

    terminateGlobal(false);
    synchronized (syncState) {
      syncState.notifyAll(); // thread is done
    }
  }

  public boolean start() {
    synchronized (syncState) { // start() should be called at most once
      if (state != State.NEW) {
        return false;
      }
      state = State.STARTED;
    }

    new Thread(this::doStart).start();
    return true;
  }

  public void stop() throws InterruptedException {
    synchronized (syncState) {
      if (state != State.OPEN) {
        if (state == State.NEW || state == State.STARTED) {
          state = State.FINISHED;
        }
        return;
      }
      state = State.FINISHING;
      while (state != State.FAILED && state != State.FINISHED) {
        syncState.wait();
      }
    }
  }

  void registerAccept(Consumer<ConnectionId> ls) {
    this.lsAccept = ls;
  }

  void registerGlobalFail(Consumer<String> ls) {
    this.lsFail = ls;
  }

  void register(ConnectionId cid, BiConsumer<ConnectionId, NetworkMessage> ls) {
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx != null) {
      ctx.setListener(ls);
    }
  }

  private ConnectionContext getContextFrom(ConnectionId cid) {
    for (ConnectionContext ctx : contexts) {
      if (ctx.getCid() == cid) {
        return ctx;
      }
    }
    return null;
  }

  private ConnectionContext getContextFrom(SocketChannel chan) {
    for (ConnectionContext ctx : contexts) {
      if (ctx.getChannel() == chan) {
        return ctx;
      }
    }
    return null;
  }

  public void send(ConnectionId cid, Byte type, Object obj) throws IOException {
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx == null) {
      // this shouldn't be possible, as we don't remove old contexts
      throw new IOException("invalid connection ID");
    }
    if (!ctx.getChannel().isConnected()) {
      throw new IOException("no socket open for this connection ID");
    }
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    byteOut.write(type);
    try {
      bp.serialize(type, obj, byteOut);
    } catch (SerializationException | ClassCastException e) {
      // fail this connection, but not the whole manager
      ctx.close(true);
      throw new IOException("serialization error");
    } catch (IOException e) {
      ctx.close(true);
      throw e;
    }
    try {
      ctx.write(ByteBuffer.wrap(byteOut.toByteArray()));
    } catch (IOException e) {
      // fail this connection
      ctx.close(true);
      throw e;
    }
  }
}
