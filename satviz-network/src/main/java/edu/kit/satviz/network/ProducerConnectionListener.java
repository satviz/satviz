package edu.kit.satviz.network;

/**
 * Callback methods for the producer side listening on a consumer connection.
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
