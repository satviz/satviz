package edu.kit.satviz.network;

/**
 * A bunch of methods for a listener on a producer connection.
 */
public interface ProducerConnectionListener {

  /**
   * Called when the connection to the server was established.
   */
  void onConnect();

  /**
   * Called when the connection to the server was closed.
   *
   * @param reason the reason for closing
   */
  void onDisconnect(String reason);
}
