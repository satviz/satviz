package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerializationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A connection manager to send network messages.
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

  // TODO check these access rights
  private final Object syncState = new Object();
  private volatile State state = State.NEW;

  private final NetworkBlueprint bp;
  private final ByteBuffer readBuf = ByteBuffer.allocate(1024);

  private Consumer<ConnectionId> lsConnect;
  private Consumer<String> lsFail;

  protected AbstractConnectionManager(NetworkBlueprint bp) {
    this.bp = bp;
  }

  protected final void terminateGlobal(boolean abnormal, String reason) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return;
      }
      state = abnormal ? State.FAILED : State.FINISHED;

      processTerminateGlobal(abnormal, reason);

      if (state == State.FAILED && lsFail != null) {
        lsFail.accept(reason);
      }
    }
  }

  public boolean isClosed() {
    synchronized (syncState) {
      return state == State.FINISHED || state == State.FAILED;
    }
  }

  public final boolean start() {
    synchronized (syncState) { // start() should be called at most once
      if (state != State.NEW) {
        return false;
      }
      state = State.STARTED;
    }

    new Thread(this::processStart).start();
    return true;
  }

  public final void stop() throws InterruptedException {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return;
      }
      if (state == State.NEW) {
        // thread has not been created
        processStopNew();
      }

      state = State.FINISHING; // let reader thread handle terminating
      while (state != State.FAILED && state != State.FINISHED) {
        // while loop to catch spurious wakeup
        syncState.wait();
      }
    }
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

  public final void register(ConnectionId cid, BiConsumer<ConnectionId, NetworkMessage> ls) {
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx != null) {
      ctx.setListener(ls);
    }
  }

  public final void send(ConnectionId cid, Byte type, Object obj) throws IOException {
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx == null || ctx.isClosed()) {
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
    } catch (NotYetConnectedException e) {
      throw e; // don't terminate; avoid being caught by next block
    } catch (IOException e) {
      ctx.close(true);
      throw e;
    }
  }

  protected abstract void processTerminateGlobal(boolean abnormal, String reason);

  protected abstract void processStart();

  protected abstract void processStopNew();

  protected abstract ConnectionContext getContextFrom(ConnectionId cid);
}
