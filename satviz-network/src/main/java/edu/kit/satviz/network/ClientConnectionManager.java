package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A client to connect to a {@link ConnectionManager}.
 *
 * @apiNote thread-safe
 */
public class ClientConnectionManager {
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

  private final ConnectionContext ctx;

  private final NetworkBlueprint bp;
  private final ByteBuffer readBuf = ByteBuffer.allocate(1024);

  private Consumer<ConnectionId> lsConnect;
  private Consumer<String> lsFail;

  /**
   * Creates a new client connection with specified address and port, and message type mapping.
   * No socket is opened until <code>start()</code> is called.
   *
   * @param address the address to connect to
   * @param port the port to connect to
   * @param bp the message type mapping
   */
  public ClientConnectionManager(String address, int port, NetworkBlueprint bp) {
    this.bp = bp;
    this.ctx = new ConnectionContext(
        new ConnectionId(new InetSocketAddress(address, port)),
        new Receiver(bp::getBuilder),
        null
    );
  }

  private void terminateGlobal(boolean abnormal) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return;
      }
      state = abnormal ? State.FAILED : State.FINISHED;

      ctx.close(abnormal);
      if (state == State.FAILED && lsFail != null) {
        lsFail.accept("fail");
      }
    }
  }

  // TODO make sure a handler calling a method can't do anything stupid (it holds some locks!)
  // this also affects the calls to ctx


  /**
   * Returns whether the client has finished.
   *
   * @return whether it has finished or not
   */
  public boolean isClosed() {
    synchronized (syncState) {
      return state == State.FINISHED || state == State.FAILED;
    }
  }

  private boolean doRead() {
    try {
      ctx.read(readBuf);
    } catch (IOException e) {
      ctx.close(true);
      return false;
    }
    return true;
  }

  private void doStart() {
    synchronized (syncState) {
      while (state == State.STARTED) {
        try {
          if (ctx.tryConnect()) {
            state = State.OPEN;
          }
        } catch (IOException e) {
          terminateGlobal(true);
          return;
        }
        try {
          syncState.wait(1000);
          // state might change in the meantime
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          terminateGlobal(true);
          return;
        }
      }

      if (state == State.OPEN && lsConnect != null) {
        lsConnect.accept(ctx.getCid());
      }
    }

    while (state == State.OPEN) {
      if (!doRead()) {
        terminateGlobal(true);
      }
    }

    terminateGlobal(false);
    synchronized (syncState) {
      syncState.notifyAll(); // thread is done
    }
  }

  /**
   * Starts this client connection.
   * A new thread is created that opens the socket and reads data.
   *
   * @return true if the new thread was created, false if it already existed
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
   * Stops this client connection.
   * Waits on the reading thread to finish and close all resources.
   *
   * @throws InterruptedException if the waiting gets interrupted
   */
  public void stop() throws InterruptedException {
    synchronized (syncState) {
      if (state == State.NEW) {
        // other thread was not started yet
        state = State.FINISHED;
        ctx.close(false);
        return;
      }

      if (state != State.STARTED && state != State.OPEN) {
        return;
      }

      state = State.FINISHING;
      while (state != State.FAILED && state != State.FINISHED) {
        // while loop to catch spurious wakeup
        syncState.wait();
      }
    }
  }

  /**
   * Registers a listener that is called when the connection has been established.
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

  /**
   * Registers a listener that is called when network messages arrive from the server.
   *
   * @param ls the listener
   */
  public void register(BiConsumer<ConnectionId, NetworkMessage> ls) {
    ctx.setListener(ls);
  }

  /**
   * Sends a message with an object to the server.
   *
   * @param type the message type
   * @param obj the object to transport with this message
   * @throws NotYetConnectedException if the socket is not yet connected
   * @throws IOException if the socket is closed or the serialization fails
   */
  public void send(Byte type, Object obj) throws IOException {
    if (ctx.isClosed()) {
      throw new IOException("socket is closed");
    }

    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    byteOut.write(type);
    try {
      bp.serialize(type, obj, byteOut);
    } catch (SerializationException | ClassCastException e) {
      terminateGlobal(true);
      throw new IOException("serialization error");
    } catch (IOException e) {
      terminateGlobal(true);
      throw e;
    }
    try {
      ctx.write(ByteBuffer.wrap(byteOut.toByteArray()));
    } catch (NotYetConnectedException e) {
      throw e; // don't terminate; avoid being caught by next block
    } catch (IOException e) {
      terminateGlobal(true);
      throw e;
    }
  }
}
