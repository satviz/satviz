package edu.kit.satviz.producer.source;

import static edu.kit.satviz.producer.ResourceHelper.PROOF_UPDATES;
import static edu.kit.satviz.producer.ResourceHelper.extractResource;
import static org.junit.jupiter.api.Assertions.*;

import edu.kit.satviz.producer.ProducerModeData;
import edu.kit.satviz.producer.SourceException;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.mode.ProofMode;
import edu.kit.satviz.sat.ClauseUpdate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProofSourceTest {

  private ProofMode mode;

  @BeforeEach
  void setUp() {
    mode = new ProofMode();
  }

  @Test
  void test_open_finish() throws IOException, SourceException {
    var params = new ProducerParameters();
    params.setHost("example.com");
    params.setProofFile(extractResource("/proof.drat"));
    ProducerModeData data = mode.apply(params);
    var bool = new AtomicBoolean(false);
    data.source().whenRefuted(() -> bool.set(true));
    data.source().open();
    assertTrue(bool.get());
  }

  @Test
  void test_open_clauses() throws IOException, SourceException {
    var params = new ProducerParameters();
    params.setHost("example.com");
    params.setProofFile(extractResource("/proof.drat"));
    ProducerModeData data = mode.apply(params);
    List<ClauseUpdate> updates = new ArrayList<>();
    data.source().subscribe(updates::add);
    data.source().open();
    assertEquals(PROOF_UPDATES, updates);
  }

}
