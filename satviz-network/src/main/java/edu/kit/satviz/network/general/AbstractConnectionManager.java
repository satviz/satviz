package edu.kit.satviz.network.general;

import edu.kit.satviz.serial.SerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An abstract connection manager to send network messages.
 * This class manages a set of connection contexts with methods to read from and write to them.
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
  protected final Object syncListen = new Object();
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
      for (ConnectionContext ctx : contexts) {
        // no new contexts are added while this loop executes
        ctx.close(abnormal);
      }
      processTerminateGlobal(abnormal, reason);

      if (abnormal) {
        callFail(reason);
      }
    }
  }

  /**
   * Adds a context to this manager's selector with <code>OP_READ</code> interest set,
   *     and adds it to the set of contexts.
   * Also calls the method listening on new connections.
   *
   * @param ctx the context to add
   * @return true if the context was added, false if an error was encountered
   */
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
      callConnect(ctx.getCid());
      return true;
    }
  }

  /**
   * Creates a new context from the given channel and adds it to this manager.
   * The channel is set to non-blocking. Do not change this after calling this method.
   *
   * @param chan the channel of which to create a new context
   * @return true if the context was added, false if an error was encountered
   */
  protected boolean addContext(SocketChannel chan) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return false;
      }
      try {
        chan.configureBlocking(false); // non-blocking to use in selector
      } catch (IOException e) {
        return false;
      }
      ConnectionContext newCtx = new ConnectionContext(
          chan,
          new Receiver(bp::getBuilder),
          null
      );
      return addContext(newCtx);
    }
  }

  /**
   * Adds a channel to this manager's selector with <code>OP_ACCEPT</code> interest set.
   * Used for custom server socket behaviour.
   *
   * @param chan the channel to add to the selector
   * @return true if successful, false otherwise
   */
  protected boolean addAcceptable(AbstractSelectableChannel chan) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return false;
      }
      try {
        chan.register(sel, SelectionKey.OP_ACCEPT);
      } catch (ClosedChannelException e) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns whether this manager has terminated or not.
   *
   * @return whether it has terminated or not
   */
  public boolean isClosed() {
    synchronized (syncState) {
      return state == State.FINISHED || state == State.FAILED;
    }
  }

  /**
   * Starts this manager.
   * A new thread is created in the background that services incoming data on sockets.
   * Calling this method more than once has no effect.
   *
   * @return whether this was the first time calling this method or not
   */
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

  /**
   * Stops this manager.
   * This is an asynchronous operation. The background thread is tasked with cleaning up.
   * Call <code>finishStop()</code> to wait until the background thread has terminated.
   */
  public final void stop() {
    synchronized (syncState) {
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
    }
  }

  /**
   * Stops this manager and waits for it to terminate and release all its resources.
   *
   * @throws InterruptedException if this thread was interrupted while waiting
   */
  public final void finishStop() throws InterruptedException {
    stop();
    synchronized (syncState) {
      while (state != State.FAILED && state != State.FINISHED) {
        // we won't enter this loop if the background thread is already done or was never created
        syncState.wait();
      }
    }
  }

  private void doRead(ConnectionContext ctx) {
    if (ctx == null) {
      return;
    }
    try {
      ctx.read(readBuf);
    } catch (IOException e) {
      ctx.close(true);
    }
  }

  /**
   * Registers a listener that is called when a connection has been established.
   *
   * @param ls the listener
   */
  public final void registerConnect(Consumer<ConnectionId> ls) {
    synchronized (syncListen) {
      this.lsConnect = ls;
    }
  }

  /**
   * Registers a listener that is called when the manager fails unexpectedly.
   *
   * @param ls the listener
   */
  public final void registerGlobalFail(Consumer<String> ls) {
    synchronized (syncListen) {
      this.lsFail = ls;
    }
  }

  /**
   * Sets a listener for a given connection on this manager.
   *
   * @param cid the ID of the connection
   * @param ls the listener
   * @return whether the ID has a corresponding context in this manager
   */
  public final boolean register(ConnectionId cid, BiConsumer<ConnectionId, NetworkMessage> ls) {
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx != null) {
      ctx.setListener(ls); // this is synchronized internally
      return true;
    }
    return false;
  }

  protected final void callConnect(ConnectionId cid) {
    synchronized (syncListen) {
      if (lsConnect != null) {
        lsConnect.accept(cid);
      }
    }
  }

  private void callFail(String reason) {
    synchronized (syncListen) {
      if (lsFail != null) {
        lsFail.accept(reason);
      }
    }
  }

  private void pollAll() {
    synchronized (syncState) {
      if (state != State.OPEN) {
        return;
      }

      try {
        sel.select(1000);
      } catch (IOException e) {
        terminateGlobal(true, "error selecting socket events");
        return;
      }
    }
    Iterator<SelectionKey> iter = sel.selectedKeys().iterator();
    while (iter.hasNext()) {
      SelectionKey key = iter.next();
      if (key.isAcceptable()) {
        processSelectAcceptable(key);
      } else if (key.isReadable()) {
        doRead(getContextFrom((SocketChannel) key.channel()));
      }
      iter.remove();
    }
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
   * Sends a network message to over a given connection on this manager.
   *
   * @param cid the ID of the connection
   * @param type the message type
   * @param obj the object to transport with the message
   * @throws IllegalStateException if the context is currently not connected
   * @throws IOException if the serialization or sending fails. In this case, the context is closed.
   */
  public final void send(ConnectionId cid, Byte type, Object obj) throws IOException {
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx == null || ctx.isClosed()) {
      throw new IllegalStateException("context is closed or not yet connected");
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

  protected final ConnectionContext getContextFrom(ConnectionId cid) {
    for (ConnectionContext ctx : contexts) {
      if (ctx.getCid() == cid) {
        return ctx;
      }
    }
    return null;
  }

  protected final ConnectionContext getContextFrom(SocketChannel chan) {
    for (ConnectionContext ctx : contexts) {
      if (ctx.usesChannel(chan)) {
        return ctx;
      }
    }
    return null;
  }

  /**
   * Closes a connection context associated with the given ID.
   * Closing is silent, which means that the context listener will not be notified.
   *
   * @param cid the ID of the connection
   */
  public final void close(ConnectionId cid) {
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx != null) {
      ctx.closeSilent(false);
    }
  }

  protected abstract void processTerminateGlobal(boolean abnormal, String reason);

  protected abstract void processStart(); // synchronize as needed

  protected abstract void processSelectAcceptable(SelectionKey key); // synchronize as needed
}
