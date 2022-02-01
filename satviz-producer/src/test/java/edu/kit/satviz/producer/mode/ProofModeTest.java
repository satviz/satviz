package edu.kit.satviz.producer.mode;

import edu.kit.satviz.producer.SourceException;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.source.ProofSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static edu.kit.satviz.producer.ResourceHelper.extractResource;
import static org.junit.jupiter.api.Assertions.*;

class ProofModeTest {

  private ProofMode mode;

  @BeforeEach
  void setUp() {
    mode = new ProofMode();
  }

  @Test
  void test_isSet_yes() {
    var params = new ProducerParameters();
    params.setHost("example.com");
    params.setProofFile(Paths.get("proof.drat"));
    assertTrue(mode.isSet(params));
  }

  @Test
  void test_isSet_no() {
    var params = new ProducerParameters();
    params.setHost("example.com");
    assertFalse(mode.isSet(params));
  }

  @Test
  void test_createSource_correctType() throws IOException {
    var proof = extractResource("/proof.drat");
    var params = new ProducerParameters();
    params.setHost("example.com");
    params.setProofFile(proof);
    assertTrue(mode.isSet(params));
    try {
      var source = mode.createSource(params);
      assertTrue(source instanceof ProofSource);
    } catch (SourceException e) {
      fail("valid source couldn't be created", e);
    }
    Files.delete(proof);
  }

}
