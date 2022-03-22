package edu.kit.satviz.producer;

import static edu.kit.satviz.producer.ResourceHelper.PROOF_UPDATES;
import static edu.kit.satviz.producer.ResourceHelper.extractResource;
import static edu.kit.satviz.producer.SolverParams.solverParams;
import static org.mockito.Mockito.*;

import edu.kit.satviz.network.pub.ProducerConnection;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.mode.ProofMode;
import edu.kit.satviz.producer.mode.SolverMode;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SourceControlConnectionListenerTest {

  private ProducerConnection connection;

  @BeforeAll
  static void createTempDir() throws IOException {
    ResourceHelper.createTempDir();
  }

  @AfterAll
  static void deleteTempDir() throws IOException {
    ResourceHelper.deleteTempDir();
  }

  @BeforeEach
  void setUp() {
    connection = mock(ProducerConnection.class);
  }

  @Test
  void test_onConnect_sourceException() throws IOException, SourceException, InterruptedException {
    var params = new ProducerParameters();
    params.setHost("example.com");
    params.setProofFile(extractResource("/instance-broken.cnf"));
    ProducerModeData data = new ProofMode().apply(params);
    var listener = new SourceControlConnectionListener(connection, data.source());
    listener.onConnect();
    listener.getSourceThread().join();
    verify(connection).terminateOtherwise(anyString());
  }

  @Test
  void test_onConnect_refutedResult() throws InterruptedException, SourceException, IOException {
    var params = solverParams("/libcadical.so", "/instance-unsat.cnf");
    ProducerModeData data = new SolverMode().apply(params);
    var listener = new SourceControlConnectionListener(connection, data.source());
    listener.onConnect();
    listener.getSourceThread().join();
    verify(connection).terminateRefuted();
  }

  @Test
  void test_onConnect_satisfiableResult() throws IOException, SourceException, InterruptedException {
    var params = solverParams("/libcadical.so", "/instance.cnf");
    ProducerModeData data = new SolverMode().apply(params);
    var listener = new SourceControlConnectionListener(connection, data.source());
    listener.onConnect();
    listener.getSourceThread().join();
    verify(connection).terminateSolved(notNull());
  }

  @Test
  void test_onConnect_clausesSent() throws IOException, SourceException, InterruptedException {
    var params = new ProducerParameters();
    params.setHost("example.com");
    params.setProofFile(extractResource("/proof.drat"));
    ProducerModeData data = new ProofMode().apply(params);
    var listener = new SourceControlConnectionListener(connection, data.source());
    listener.onConnect();
    listener.getSourceThread().join();
    PROOF_UPDATES.forEach(update -> verify(connection).sendClauseUpdate(update));
    verify(connection, times(PROOF_UPDATES.size())).sendClauseUpdate(notNull());
    verify(connection).terminateRefuted();
  }

}
