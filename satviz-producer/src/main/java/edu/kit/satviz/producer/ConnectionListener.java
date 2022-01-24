package edu.kit.satviz.producer;

import edu.kit.satviz.network.ProducerConnection;
import edu.kit.satviz.network.ProducerConnectionListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionListener implements ProducerConnectionListener {

  private static final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final ProducerConnection connection;
  private final ClauseSource source;

  public ConnectionListener(ProducerConnection connection, ClauseSource source) {
    this.connection = connection;
    this.source = source;
    source.subscribe(connection::sendClauseUpdate);
    source.whenSolved(connection::terminateSolved);
    source.whenRefuted(connection::terminateRefuted);
  }

  @Override
  public void onConnect() {
    executor.submit(() -> {
      try (source) {
        source.open();
      } catch (SourceOpeningException e) {
        e.printStackTrace();
        connection.terminateFailed(e.getMessage());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

  }

  @Override
  public void onDisconnect(String reason) {
    try {
      source.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
