package edu.kit.satviz.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.function.BiConsumer;

/**
 * A Collection of objects related to a network connection.
 *
 * @apiNote this class is thread-safe
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
   * Creates a new connection context without a socket.
   * A socket can be opened and connected with <code>tryConnect</code>.
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
   * Creates a new connection context with previously opened socket.
   *
   * @param cid the ID of this connection
   * @param chan the socket channel
   * @param recv the receiver
   * @param ls the listener
   */
  public ConnectionContext(ConnectionId cid, SocketChannel chan, Receiver recv,
      BiConsumer<ConnectionId, NetworkMessage> ls) {
    this.cid = cid;
    this.chan = chan;
    this.recv = recv;
    this.ls = ls;
    this.state = State.CONNECTED;
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
   * Sends the corresponding message if the connection has already failed or terminated.
   *
   * @param ls the listener
   */
  public synchronized void setListener(BiConsumer<ConnectionId, NetworkMessage> ls) {
    this.ls = ls;
    if (state == State.FAILED) {
      ls.accept(cid, NetworkMessage.createFail());
    } else if (state == State.FINISHED) {
      ls.accept(cid, NetworkMessage.createTerm());
    }
  }

  /**
   * Sets the listener of this connection if no listener was registered before.
   *
   * @param ls the listener
   * @return true if the assignment was successful, false otherwise
   */
  public synchronized boolean trySetListener(BiConsumer<ConnectionId, NetworkMessage> ls) {
    if (this.ls == null) {
      setListener(ls);
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
  public synchronized void close(boolean abnormal) {
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
  public synchronized boolean tryConnect() throws IOException {
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
    // not synchronized since every other method is
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
   * @throws NotYetConnectedException if the socket is not connected
   * @throws IOException if some other socket or messaging
   */
  public synchronized void read(ByteBuffer bb) throws IOException {
    if (state != State.CONNECTED) {
      throw new NotYetConnectedException();
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
        if (msg.getState() == NetworkMessage.State.FAIL) {
          close(true);
          throw new IOException("message conversion error");
        }
        callListener(msg);
      }
    }
    bb.clear();
  }

  /**
   * Writes the specified data to this socket channel.
   *
   * @param bb the buffer holding the data
   * @throws NotYetConnectedException if the socket is not connected
   * @throws IOException if the channel is not connected or a write error occurs
   */
  public synchronized void write(ByteBuffer bb) throws IOException {
    if (state != State.CONNECTED) {
      throw new NotYetConnectedException();
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
