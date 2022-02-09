package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An abstract connection manager to send network messages.
 * Client and server connection managers can be built from this.
 *
 * @apiNote thread-safe
 */
public abstract class AbstractConnectionManager {
  /** The states of a connection manager. */
  public enum State {
    NEW,
    STARTED,
    OPEN,
    FINISHING,
    FINISHED,
    FAILED
  }

  protected final Object syncState = new Object();
  protected volatile State state = State.NEW;

  private final NetworkBlueprint bp;
  private final Set<ConnectionContext> contexts = ConcurrentHashMap.newKeySet();
  private final ByteBuffer readBuf = ByteBuffer.allocate(1024);

  private Selector sel;

  private Consumer<ConnectionId> lsConnect = null;
  private Consumer<String> lsFail = null;

  protected AbstractConnectionManager(NetworkBlueprint bp) {
    this.bp = bp;
  }

  protected final void terminateGlobal(boolean abnormal, String reason) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return;
      }
      state = abnormal ? State.FAILED : State.FINISHED;

      if (sel != null) {
        try {
          sel.close();
        } catch (IOException e) {
          // not our problem
        }
      }
      System.out.println("processTerminateGlobal: close contexts");
      for (ConnectionContext ctx : contexts) {
        // no new contexts are added while this loop executes
        ctx.close(abnormal);
      }
      processTerminateGlobal(abnormal, reason);

      if (state == State.FAILED && lsFail != null) {
        lsFail.accept(reason);
      }
    }
  }

  protected boolean addContext(ConnectionContext ctx) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return false;
      }
      try {
        ctx.register(sel, SelectionKey.OP_READ);
      } catch (ClosedChannelException e) {
        return false;
      }
      contexts.add(ctx);
      return true;
    }
  }

  protected ConnectionContext addContext(SocketChannel chan) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return null;
      }
      try {
        chan.configureBlocking(false); // non-blocking to use in selector
      } catch (IOException e) {
        return null;
      }
      ConnectionContext newCtx = new ConnectionContext(
          chan,
          new Receiver(bp::getBuilder),
          null
      );
      try {
        newCtx.register(sel, SelectionKey.OP_READ);
      } catch (ClosedChannelException e) {
        return null;
      }

      contexts.add(newCtx);
      return newCtx;
    }
  }

  protected boolean addToSelector(AbstractSelectableChannel chan, int ops) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return false;
      }
      try {
        chan.register(sel, ops);
      } catch (ClosedChannelException e) {
        return false;
      }
    }
    return true;
  }

  public boolean isClosed() {
    synchronized (syncState) {
      return state == State.FINISHED || state == State.FAILED;
    }
  }

  public final boolean start() {
    synchronized (syncState) {
      if (state != State.NEW) {
        return false;
      }
      state = State.STARTED;
    }

    try {
      sel = Selector.open();
    } catch (IOException e) {
      terminateGlobal(true, "error creating selector");
      return false;
    }

    new Thread(this::doStart).start();
    return true;
  }

  public final void stop() {
    synchronized (syncState) {
      System.out.println("stop: got lock");
      if (state == State.FINISHING || state == State.FINISHED || state == State.FAILED) {
        return;
      }
      if (state == State.NEW) {
        // nothing has been created yet
        state = State.FINISHED;
        return;
      }

      state = State.FINISHING; // let reader thread handle terminating
      // even if reader thread has already failed at this point, it still terminates
      System.out.println("set state to FINISHING");
    }
  }

  public final void finishStop() throws InterruptedException {
    stop();
    synchronized (syncState) {
      while (state != State.FAILED && state != State.FINISHED) {
        syncState.wait();
      }
    }
  }

  // blocking for blocking sockets; only use on previously selected
  private boolean doRead(ConnectionContext ctx) {
    if (ctx == null) {
      return false;
    }
    try {
      ctx.read(readBuf);
    } catch (IOException e) {
      ctx.close(true);
      return false;
    }
    return true;
  }

  /**
   * Registers a listener that is called when a connection has been established.
   *
   * @param ls the listener
   */
  public void registerConnect(Consumer<ConnectionId> ls) {
    this.lsConnect = ls;
  }

  /**
   * Registers a listener that is called when the manager fails unexpectedly.
   *
   * @param ls the listener
   */
  public void registerGlobalFail(Consumer<String> ls) {
    this.lsFail = ls;
  }

  public void register(ConnectionId cid, BiConsumer<ConnectionId, NetworkMessage> ls) {
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx != null) {
      ctx.setListener(ls);
    } else {
      ls.accept(cid, NetworkMessage.createTerm()); // no context for this connection ID
    }
  }

  protected boolean callConnect(ConnectionId cid) {
    if (lsConnect != null) {
      lsConnect.accept(cid);
      return true;
    }
    return false;
  }

  private void pollAll() {
    System.out.println("pollAll");
    synchronized (syncState) {
      System.out.println("pollAll: got lock");
      if (state != State.OPEN) {
        System.out.println("pollAll: state not open before selecting");
        return;
      }

      try {
        sel.select(1000);
      } catch (IOException e) {
        System.out.println("selection error");
        terminateGlobal(true, "error selecting socket events");
        return;
      }
    }
    System.out.println("pollAll: selected");
    Iterator<SelectionKey> iter = sel.selectedKeys().iterator();
    while (iter.hasNext()) {
      SelectionKey key = iter.next();
      if (key.isAcceptable()) {
        processSelectAcceptable(key);
      } else if (key.isReadable()) {
        System.out.println("Server: pollAll: read event");
        doRead(getContextFrom((SocketChannel) key.channel()));
      }
      iter.remove();
    }
    System.out.println("Server: pollAll successful");
  }

  private void doStart() {
    processStart();

    while (state == State.OPEN) {
      pollAll();
    }

    terminateGlobal(false, "finished");
    synchronized (syncState) {
      syncState.notifyAll(); // thread is done
    }
  }

  /**
   *
   * @param cid
   * @param type
   * @param obj
   * @throws NotYetConnectedException if the context is currently not connected
   * @throws IOException if the serialization or sending fails. In this case, the context is closed.
   */
  public final void send(ConnectionId cid, Byte type, Object obj) throws IOException {
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx == null || ctx.isClosed()) {
      throw new NotYetConnectedException();
    }
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    byteOut.write(type);
    try {
      bp.serialize(type, obj, byteOut);
    } catch (SerializationException | ClassCastException e) {
      // fail this connection, but not the whole manager
      ctx.close(true);
      throw new IOException("serialization error:" + e);
    } catch (IOException e) {
      ctx.close(true);
      throw e;
    }
    try {
      ctx.write(ByteBuffer.wrap(byteOut.toByteArray()));
    } catch (NotYetConnectedException e) {
      throw e; // don't terminate
    } catch (IOException e) {
      ctx.close(true);
      throw e;
    }
  }

  protected ConnectionContext getContextFrom(ConnectionId cid) {
    for (ConnectionContext ctx : contexts) {
      if (ctx.getCid() == cid) {
        return ctx;
      }
    }
    return null;
  }

  protected ConnectionContext getContextFrom(SocketChannel chan) {
    for (ConnectionContext ctx : contexts) {
      if (ctx.usesChannel(chan)) {
        return ctx;
      }
    }
    return null;
  }

  protected abstract void processTerminateGlobal(boolean abnormal, String reason);

  protected abstract void processStart(); // synchronize as needed

  protected abstract void processSelectAcceptable(SelectionKey key); // synchronize as needed
}
