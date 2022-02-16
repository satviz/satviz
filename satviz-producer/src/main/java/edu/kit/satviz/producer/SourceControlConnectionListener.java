package edu.kit.satviz.producer;

import edu.kit.satviz.network.ProducerConnection;
import edu.kit.satviz.network.ProducerConnectionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code ProducerConnectionListener} used by this application.<br>
 * Its purpose is to open/close a {@link ClauseSource} when a consumer connects/disconnects and
 * forward updates in the {@code source} to the network connection.
 */
public class SourceControlConnectionListener implements ProducerConnectionListener {

  private static final Logger logger = Logger.getLogger("Network");

  private final ProducerConnection connection;
  private final ClauseSource source;

  private Thread sourceThread;

  /**
   * Creates a new {@code SourceControlConnectionListener}.
   *
   * @param connection The {@code ProducerConnection} used to send messages to a consumer
   * @param source The underlying {@link ClauseSource}.
   */
  public SourceControlConnectionListener(ProducerConnection connection, ClauseSource source) {
    this.connection = connection;
    this.source = source;
    source.subscribe(connection::sendClauseUpdate);
    source.whenSolved(connection::terminateSolved);
    source.whenRefuted(connection::terminateRefuted);
  }

  /*
   * Opens the {@code source} on a separate thread, terminating the connection if something
   * goes wrong.
   */
  @Override
  public void onConnect() {
    logger.info("Connected to consumer");
    sourceThread = new Thread(null, () -> {
      try (source) {
        logger.info("Starting to send clause updates");
        source.open();
      } catch (SourceException e) {
        logger.log(Level.SEVERE, "Error draining source", e);
        connection.terminateFailed(e.getMessage());
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Unknown error", e);
      }
    }, "ClauseSource-" + Integer.toHexString(source.hashCode()));
    sourceThread.start();
  }

  /*
   * Closes the underlying source.
   */
  @Override
  public void onDisconnect(String reason) {
    logger.log(Level.WARNING, "Consumer connection disconnected. Reason: {}", reason);
    try {
      source.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the thread on which the {@link ClauseSource} was opened.
   *
   * @return the source thread or {@code null} if {@link #onConnect()} has not been called yet
   */
  public Thread getSourceThread() {
    return sourceThread;
  }
}
