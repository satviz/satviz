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

  private final ConnectionId cid;
  private final SocketChannel chan;
  private final Receiver recv;
  private BiConsumer<ConnectionId, NetworkMessage> ls;

  /**
   * Creates a new connection context with all attributes set.
   *
   * @param cid the ID of this connection
   * @param recv the receiver
   * @param ls the listener
   * @throws IOException if a socket channel cannot be opened
   */
  public ConnectionContext(ConnectionId cid, Receiver recv,
      BiConsumer<ConnectionId, NetworkMessage> ls) throws IOException {
    this.cid = cid;
    this.chan = SocketChannel.open();
    this.recv = recv;
    this.ls = ls;
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

  private void quit(boolean abnormal) {
    try {
      chan.close();
    } catch (IOException e) {
      // everything is alright
    }
    callListener(abnormal ? NetworkMessage.createFail() : NetworkMessage.createTerm());
  }

  /**
   * Tries to connect the socket channel.
   * The behaviour is the same for blocking and non-blocking sockets.
   *
   * @return true if the connection is established, false if no-one is listening on the remote port
   * @throws IOException if any I/O error occurs
   */
  public boolean tryConnect() throws IOException {
    try {
      if (!chan.connect(cid.address())) {
        chan.finishConnect(); // was in non-blocking mode
      }
    } catch (ConnectException e) {
      // server not yet online
      return false;
    }
    return true;
  }

  /**
   * Tells whether the socket channel is connected.
   *
   * @return true if the socket channel is connected, false otherwise
   */
  public boolean isConnected() {
    return chan.isConnected();
  }

  /**
   * Calls the listener in this context with a message.
   *
   * @param msg the message
   * @return true if a listener was messaged, false otherwise
   */
  public boolean callListener(NetworkMessage msg) {
    if (ls != null) {
      ls.accept(cid, msg);
      return true;
    }
    return false;
  }

  /**
   * Gets the output stream associated with this channel.
   *
   * @return the output stream
   * @throws IOException if an I/O error occurs or the socket is not connected
   */
  public OutputStream getOutputStream() throws IOException {
    return chan.socket().getOutputStream();
  }

  public void readAndConvert(ByteBuffer bb) throws IOException {
    if (chan.read(bb) == -1) {
      // something went wrong?
      chan.close();
    }
    bb.flip();
    while (bb.hasRemaining()) {
      NetworkMessage msg = recv.receive(bb);
      callListener(msg);
    }
  }
}
