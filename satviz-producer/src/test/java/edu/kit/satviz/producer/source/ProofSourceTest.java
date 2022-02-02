package edu.kit.satviz.producer.source;

import static edu.kit.satviz.producer.ResourceHelper.extractResource;
import static org.junit.jupiter.api.Assertions.*;

import edu.kit.satviz.producer.SourceException;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.mode.ProofMode;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
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
    var source = mode.createSource(params);
    var bool = new AtomicBoolean(false);
    source.whenRefuted(() -> bool.set(true));
    source.open();
    assertTrue(bool.get());
  }

  @Test
  void test_open_clauses() throws IOException, SourceException {
    var params = new ProducerParameters();
    params.setHost("example.com");
    params.setProofFile(extractResource("/proof.drat"));
    var source = mode.createSource(params);
    List<ClauseUpdate> updates = new ArrayList<>();
    source.subscribe(updates::add);
    var expected = List.of(
        new ClauseUpdate(new Clause(new int[] {1, 2, 3}), ClauseUpdate.Type.ADD),
        new ClauseUpdate(new Clause(new int[] {3, -4}), ClauseUpdate.Type.REMOVE),
        new ClauseUpdate(new Clause(new int[] {2, -1}), ClauseUpdate.Type.ADD)
    );
    source.open();
    assertEquals(expected, updates);
  }

}
