package edu.kit.satviz.network;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.function.BiConsumer;

/**
 * A Collection of objects related to a network connection.
 *
 * @apiNote thread-safe
 */
public class ConnectionContext {
  private enum State {
    NEW,
    CONNECTED,
    FAILED,
    FINISHED
  }

  private final ConnectionId cid;
  private SocketChannel chan;
  private final Receiver recv;
  private BiConsumer<ConnectionId, NetworkMessage> ls;

  private final Object syncState = new Object();
  private final Object syncRead = new Object();
  private final Object syncWrite = new Object();
  private State state;

  /**
   * Creates a new connection context without a socket.
   * A socket can be opened and connected with <code>tryConnect</code>.
   *
   * @param cid the ID of this connection, including remote address
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
   * Creates a new connection context with previously opened and connected socket.
   * The socket channel must be in non-blocking mode.
   *
   * @param chan the socket channel
   * @param recv the receiver
   * @param ls the listener
   * @throws IllegalArgumentException if the channel is closed or not connected or blocking
   */
  public ConnectionContext(SocketChannel chan, Receiver recv,
      BiConsumer<ConnectionId, NetworkMessage> ls) throws IllegalArgumentException {
    InetSocketAddress remote;
    if (chan.isBlocking()) {
      throw new IllegalArgumentException("blocking socket channel");
    }
    try {
      remote = (InetSocketAddress) chan.getRemoteAddress();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    if (remote == null) {
      throw new IllegalArgumentException("socket is not connected");
    }

    this.cid = new ConnectionId(remote);
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
   * Checks if this context was created with the specified channel.
   *
   * @param chan the socket channel
   * @return true if this context's channel is <code>chan</code>, false otherwise
   */
  public boolean usesChannel(SocketChannel chan) {
    // we use this to identify contexts without having to use a getter for chan
    // in most cases we don't want to expose chan (first constructor)
    return this.chan == chan;
  }

  /**
   * Registers the socket channel of this context to a selector.
   *
   * @param sel the selector
   * @param ops the operations to listen on
   * @throws ClosedChannelException if the channel is closed
   */
  public void register(Selector sel, int ops) throws ClosedChannelException {
    chan.register(sel, ops);
  }

  /**
   * Sets the listener of this connection.
   * Sends the corresponding message if the connection has already failed or terminated.
   *
   * @param ls the listener
   */
  public void setListener(BiConsumer<ConnectionId, NetworkMessage> ls) {
    synchronized (syncRead) {
      // make sure we are not reading at the same time
      synchronized (syncState) {
        this.ls = ls;
        if (state == State.FAILED) {
          callListener(NetworkMessage.createFail());
        } else if (state == State.FINISHED) {
          callListener(NetworkMessage.createTerm());
        }
      }
    }
  }

  /**
   * Closes the connection context, including the socket channel.
   * Sends either a fail or termination message to the listener.
   * Returns immediately if this context is already closed.
   *
   * @param abnormal whether the close is abnormal or planned
   */
  public void close(boolean abnormal) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return;
      }
      state = (abnormal ? State.FAILED : State.FINISHED);

      if (chan != null) {
        try {
          chan.close();
        } catch (IOException e) {
          // if the channel doesn't want to be closed that's not our problem
        }
      }

      callListener(abnormal ? NetworkMessage.createFail() : NetworkMessage.createTerm());
    }
  }

  /**
   * Returns whether this connection is closed or not.
   *
   * @return whether it is closed or not
   */
  public boolean isClosed() {
    synchronized (syncState) {
      return state == State.FINISHED || state == State.FAILED;
    }
  }

  /**
   * Tries to connect the socket channel.
   * The behaviour is the same for blocking and non-blocking sockets.
   * Call this method several times until the connection succeeds.
   *
   * @return true if the connection is established, false if no-one is listening on the remote port
   * @throws IllegalStateException if the context was connected before
   * @throws IOException if any I/O error occurs
   */
  public boolean tryConnect() throws IOException {
    synchronized (syncState) {
      if (state != State.NEW) {
        throw new IllegalStateException("context has been connected before");
      }

      try {
        chan = SocketChannel.open();
        chan.configureBlocking(false);
      } catch (IOException e) {
        close(true);
        throw e;
      }

      try {
        if (!chan.connect(cid.address())) {
          chan.finishConnect(); // for non-blocking
        }
      } catch (ConnectException e) {
        // server not yet online; socket was closed
        // may try again later when the server is online
        return false;
      } catch (IOException e) {
        close(true);
        throw e;
      }

      state = State.CONNECTED;
      return true;
    }
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
   * @return the number of bytes read, -1 if end-of-stream
   * @throws IOException if the socket is not connected or some other messaging error occurs
   */
  public int read(ByteBuffer bb) throws IOException {
    synchronized (syncRead) { // we don't want multiple reads to happen at the same time
      bb.clear();
      int numRead;
      try {
        numRead = chan.read(bb);
      } catch (IOException e) {
        close(true);
        throw e;
      }
      if (numRead == -1) {
        // stream closed?
        close(false);
        return -1;
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
      return numRead;
    }
  }

  /**
   * Writes the specified data to this socket channel.
   *
   * @param bb the buffer holding the data
   * @return the number of bytes written
   * @throws IOException if the channel is not connected or a write error occurs
   */
  public int write(ByteBuffer bb) throws IOException {
    synchronized (syncWrite) { // we don't want multiple writes to happen at the same time
      int remaining = bb.remaining();
      while (bb.hasRemaining()) { // force synchronous
        try {
          chan.write(bb);
        } catch (IOException e) {
          close(true);
          throw e;
        }
      }
      return remaining;
    }
  }
}