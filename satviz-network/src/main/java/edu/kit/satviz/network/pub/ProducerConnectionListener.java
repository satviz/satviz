package edu.kit.satviz.network.pub;

/**
 * Callback methods for the producer side listening on a consumer connection.
 */
public interface ProducerConnectionListener {

  /**
   * Called when the connection to the server was established.
   */
  default void onConnect() {
    // do nothing
  }

  /**
   * Called when the connection to the server was closed.
   *
   * @param reason the reason for closing
   */
  default void onDisconnect(String reason) {
    // do nothing
  }
}
