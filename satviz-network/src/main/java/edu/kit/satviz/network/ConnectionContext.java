package edu.kit.satviz.network;

import java.nio.channels.SocketChannel;
import java.util.function.BiConsumer;

/**
 * A Collection for objects related to a network connection.
 *
 * @author luwae
 */
public class ConnectionContext {
  private ConnectionId cid;
  private SocketChannel chan;
  private Receiver recv;
  private BiConsumer<ConnectionId, NetworkObject> ls;

  /**
   * Creates a new connection context with all attributes set.
   *
   * @param cid the ID of this connection
   * @param chan the socket channel
   * @param recv the receiver
   * @param ls the listener
   */
  public ConnectionContext(ConnectionId cid, SocketChannel chan, Receiver recv,
      BiConsumer<ConnectionId, NetworkObject> ls) {
    this.cid = cid;
    this.chan = chan;
    this.recv = recv;
    this.ls = ls;
  }

  /**
   * Creates a new empty connection context.
   */
  public ConnectionContext() {
    cid = null;
    chan = null;
    recv = null;
    ls = null;
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
   * Sets the ID of this connection.
   *
   * @param cid the ID
   */
  public void setCid(ConnectionId cid) {
    this.cid = cid;
  }

  /**
   * Gets the channel of this connection.
   *
   * @return the channel
   */
  public SocketChannel getChan() {
    return chan;
  }

  /**
   * Sets the channel of this connection.
   *
   * @param chan the channel
   */
  public void setChan(SocketChannel chan) {
    this.chan = chan;
  }

  /**
   * Gets the receiver of this connection.
   *
   * @return the receiver
   */
  public Receiver getRecv() {
    return recv;
  }

  /**
   * Sets the receiver of this connection.
   *
   * @param recv the receiver
   */
  public void setRecv(Receiver recv) {
    this.recv = recv;
  }

  /**
   * Gets the listener of this connection.
   *
   * @return the listener
   */
  public BiConsumer<ConnectionId, NetworkObject> getLs() {
    return ls;
  }

  /**
   * Sets the listener of this connection.
   *
   * @param ls the listener
   */
  public void setLs(BiConsumer<ConnectionId, NetworkObject> ls) {
    this.ls = ls;
  }
}
