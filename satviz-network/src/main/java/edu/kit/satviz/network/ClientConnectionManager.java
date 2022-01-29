package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A client to connect to a {@link ConnectionManager}.
 *
 * @apiNote thread-safe
 * @author luwae
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

      if (state == State.STARTED || state == State.FINISHING) {
        ctx.close(abnormal);
      }
      state = abnormal ? State.FAILED : State.FINISHED;
      if (state == State.FAILED) {
        lsFail.accept("fail");
      }
    }
  }

  // TODO make sure a handler calling a method can't do anything stupid (it holds some locks!)


  /**
   * Returns whether the client has finished.
   *
   * @return whether it has finished or not
   */
  public boolean isClosed() {
    return state == State.FINISHED || state == State.FAILED;
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
      while (state != State.OPEN) {
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
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          terminateGlobal(true);
          return;
        }
      }
    }

    while (state == State.OPEN) {
      doRead();
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
      if (state == State.NEW) {
        state = State.FINISHED;
        ctx.close(false);
        return;
      }

      state = State.FINISHING;
      while (state != State.FAILED && state != State.FINISHED) {
        syncState.wait();
      }
    }
  }

  void registerAccept(Consumer<ConnectionId> ls) {
    this.lsConnect = ls;
  }

  void registerGlobalFail(Consumer<String> ls) {
    this.lsFail = ls;
  }

  void register(BiConsumer<ConnectionId, NetworkMessage> ls) {
    ctx.setListener(ls);
  }

  public void send(ConnectionId cid, Byte type, Object obj) throws IOException {
    if (!ctx.getChannel().isConnected()) {
      throw new IOException("no socket open for this connection ID");
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
    } catch (IOException e) {
      // fail this connection
      ctx.close(true);
      throw e;
    }
  }
}
