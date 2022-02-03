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

/**
 * A server servicing multiple {@link ClientConnectionManager}s.
 *
 * @apiNote thread-safe
 */
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
  private volatile State state = State.NEW; // TODO volatile?

  private final InetSocketAddress serverAddress;
  private ServerSocketChannel serverChan = null;
  private Selector select = null;

  private final Set<ConnectionContext> contexts;
  private final NetworkBlueprint bp;
  private final ByteBuffer readBuf = ByteBuffer.allocate(1024);

  private Consumer<ConnectionId> lsAccept;
  private Consumer<String> lsFail;

  /**
   * Creates a new manager with a specific port and message type mapping.
   * The socket channel is not yet opened.
   * Any client that wants to connect with this manager must possess an equal message type mapping.
   *
   * @param port the port to listen on
   * @param bp the message type mapping
   */
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
        // TODO perhaps move state change to above and use that
        ctx.close(abnormal);
      }
      state = abnormal ? State.FAILED : State.FINISHED;
      if (state == State.FAILED) {
        lsFail.accept("fail"); // TODO better message, perhaps as argument
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

  /**
   * Returns whether the manager has finished.
   * If true, no more clients can connect to this manager.
   *
   * @return whether it has finished or not
   */
  public boolean isClosed() {
    return state == State.FINISHED || state == State.FAILED;
  }

  private boolean acceptNew() {
    ConnectionContext newCtx;
    try {
      SocketChannel newChan = serverChan.accept();
      if (newChan == null) { // only call this function with acceptable event
        terminateGlobal(true);
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

  /**
   * Start this connection manager.
   * Starts a new thread in the background to listen to incoming clients and data.
   * Calling this method after the manager has finished has no effect.
   *
   * @return whether the start was successful or not
   */
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

  /**
   * Stops this connection manager.
   * Signals the background thread, which closes all sockets before terminating itself.
   * This method blocks until the background thread is terminated, to ensure safe shutdown.
   * Calling this method if the manager has finished has no effect.
   *
   * @throws InterruptedException if an interrupt occurs before the other thread terminated.
   */
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

  /**
   * Registers a new listener that gets notified if a new socket connection is accepted.
   *
   * @implNote currently, the old listener gets overwritten if this method is called twice.
   * @param ls the listener
   */
  public void registerAccept(Consumer<ConnectionId> ls) {
    this.lsAccept = ls;
  }

  /**
   * Registers a new listener that gets notified if the manager globally fails
   *     (i.e., a fatal error occurs that doesn't only affect a single connection)
   *
   * @implNote currently, the old listener gets overwritten if this method is called twice.
   * @param ls the listener
   */
  public void registerGlobalFail(Consumer<String> ls) {
    this.lsFail = ls;
  }

  /**
   * Registers a new listener for a single client connection.
   * The listener gets notified for all network messages received from this client.
   *
   * @implNote currently, the old listener gets overwritten if this method is called twice.
   * @param cid the ID of the client
   * @param ls the listener
   */
  public void register(ConnectionId cid, BiConsumer<ConnectionId, NetworkMessage> ls) {
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
      if (ctx.usesChannel(chan)) {
        return ctx;
      }
    }
    return null;
  }

  /**
   * Sends a message to the specified client.
   * If the message cannot be sent due to some error, the connection to the client is closed.
   * This does not affect the connection manager as a whole.
   *
   * @param cid the ID of the client
   * @param type the type byte of the message
   * @param obj the object to send with the message
   * @throws IOException if the socket is closed, the message cannot be serialized,
   *     or any other I/O error occurs.
   */
  public void send(ConnectionId cid, Byte type, Object obj) throws IOException {
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx == null) {
      // this shouldn't be possible, as we don't remove old contexts
      throw new IOException("invalid connection ID");
    }
    /*
    if (!ctx.getChannel().isConnected()) {
      throw new IOException("no socket open for this connection ID");
    }
    */
    // TODO removed check for now
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
