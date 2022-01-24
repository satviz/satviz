package edu.kit.satviz.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.function.BiConsumer;

/**
 * A Collection for objects related to a network connection.
 *
 * @author luwae
 */
public class ConnectionContext {
  private enum State {
    NEW,
    STARTED,
    CONNECTED,
    FAILED,
    FINISHED
  }

  private final ConnectionId cid;
  private SocketChannel chan;
  private final Receiver recv;
  private BiConsumer<ConnectionId, NetworkMessage> ls;

  private State state;

  /**
   * Creates a new connection context.
   * Does not open or connect the socket.
   *
   * @param cid the ID of this connection
   * @param recv the receiver
   * @param ls the listener
   */
  public ConnectionContext(ConnectionId cid, Receiver recv,
      BiConsumer<ConnectionId, NetworkMessage> ls) {
    this.cid = cid;
    this.recv = recv;
    this.ls = ls;
    this.state = State.NEW;
  }

  /**
   * Gets the ID of this connection.
   *
   * @return the ID
   */
  public ConnectionId getCid() {
    return cid;
  }

  /**
   * Sets the listener of this connection.
   *
   * @param ls the listener
   * @return false if a listener was already set, true otherwise
   */
  public boolean setListener(BiConsumer<ConnectionId, NetworkMessage> ls) {
    if (this.ls == null) {
      this.ls = ls;
      return true;
    }
    return false;
  }

  /**
   * Closes the connection context, including the socket channel.
   * Sends either a fail or termination message to the listener.
   *
   * @param abnormal whether the close is abnormal or planned
   */
  public void close(boolean abnormal) {
    if (state == State.FINISHED || state == State.FAILED) {
      return;
    }

    if (state == State.STARTED || state == State.CONNECTED) {
      try {
        chan.close();
      } catch (IOException e) {
        // if the channel doesn't want to be closed that's not our problem
      }
    }
    callListener(abnormal ? NetworkMessage.createFail() : NetworkMessage.createTerm());
    state = (abnormal ? State.FAILED : State.FINISHED);
  }

  /**
   * Tries to connect the socket channel.
   * The behaviour is the same for blocking and non-blocking sockets.
   * Call this method several times until the connection succeeds.
   *
   * @return true if the connection is established, false if no-one is listening on the remote port
   * @throws AlreadyConnectedException if the context was connected before
   * @throws IOException if any I/O error occurs
   */
  public boolean tryConnect() throws IOException {
    if (state == State.NEW) {
      try {
        chan = SocketChannel.open();
      } catch (IOException e) {
        close(true);
        throw e;
      }
      state = State.STARTED;
    }

    if (state != State.STARTED) {
      throw new AlreadyConnectedException();
    }

    try {
      if (!chan.connect(cid.address())) {
        chan.finishConnect(); // was in non-blocking mode
      }
    } catch (ConnectException e) {
      // server not yet online
      return false;
    } catch (IOException e) {
      close(true);
      throw e;
    }
    state = State.CONNECTED;
    return true;
  }

  private void callListener(NetworkMessage msg) {
    if (ls != null) {
      ls.accept(cid, msg);
    }
  }

  /**
   * Reads from the socket and converts the data into a stream of {@link NetworkMessage}s
   *     sent to the listener.
   * Uses the passed buffer to read bytes, so the size of the buffer determines how many bytes
   *     are read.
   * The buffer is cleared before and after using it.
   *
   * @param bb the buffer to use for reading and writing
   * @throws IOException if the channel is not connected or a serialization error occurs
   */
  public void read(ByteBuffer bb) throws IOException {
    if (state != State.CONNECTED) {
      throw new IOException("channel not connected");
    }

    bb.clear();
    try {
      if (chan.read(bb) == -1) {
        // stream closed?
        close(false);
        return;
      }
    } catch (IOException e) {
      close(true);
      throw e;
    }
    bb.flip();
    while (bb.hasRemaining()) {
      NetworkMessage msg = recv.receive(bb);
      if (msg != null) {
        callListener(msg);
        if (msg.getState() == NetworkMessage.State.FAIL) {
          close(true);
          throw new IOException("message conversion error");
        }
      }
    }
    bb.clear();
  }

  /**
   * Writes the specified data to this socket channel.
   *
   * @param bb the buffer holding the data
   * @throws IOException if the channel is not connected or a write error occurs
   */
  public void write(ByteBuffer bb) throws IOException {
    if (state != State.CONNECTED) {
      throw new IOException("channel not connected");
    }

    while (bb.hasRemaining()) { // force synchronous
      try {
        chan.write(bb);
      } catch (IOException e) {
        close(true);
        throw e;
      }
    }
  }
}
