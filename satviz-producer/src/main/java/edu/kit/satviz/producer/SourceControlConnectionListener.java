package edu.kit.satviz.producer;

import edu.kit.satviz.network.ProducerConnection;
import edu.kit.satviz.network.ProducerConnectionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The {@code ProducerConnectionListener} used by this application.<br>
 * Its purpose is to open/close a {@link ClauseSource} when a consumer connects/disconnects and
 * forward updates in the {@code source} to the network connection.
 */
public class SourceControlConnectionListener implements ProducerConnectionListener {

  private static final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final ProducerConnection connection;
  private final ClauseSource source;

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
    executor.submit(() -> {
      try (source) {
        source.open();
      } catch (SourceException e) {
        e.printStackTrace();
        connection.terminateFailed(e.getMessage());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

  }

  /*
   * Closes the underlying source.
   */
  @Override
  public void onDisconnect(String reason) {
    try {
      source.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
