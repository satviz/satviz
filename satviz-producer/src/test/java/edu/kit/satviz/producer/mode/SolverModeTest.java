package edu.kit.satviz.producer.mode;

import edu.kit.satviz.producer.SourceException;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.source.SolverSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static edu.kit.satviz.producer.ResourceHelper.extractResource;
import static org.junit.jupiter.api.Assertions.*;

class SolverModeTest {

  private SolverMode mode;

  @BeforeEach
  void setUp() {
    mode = new SolverMode();
  }

  @Test
  void test_isSet_yes() {
    var params = new ProducerParameters();
    params.setHost("example.com");
    params.setSolverFile(Paths.get("libfoo.so"));
    params.setInstanceFile(Paths.get("instance.cnf"));
    assertTrue(mode.isSet(params));
  }

  @Test
  void test_isSet_partial() {
    var params = new ProducerParameters();
    params.setHost("example.com");
    params.setSolverFile(Paths.get("libfoo.so"));
    assertFalse(mode.isSet(params));
  }

  @Test
  void test_isSet_no() {
    var params = new ProducerParameters();
    params.setHost("example.com");
    assertFalse(mode.isSet(params));
  }

  @Test
  void test_createSource_correctType() throws IOException {
    var solver = extractResource("/libcadical.so");
    var instance = extractResource("/instance.cnf");
    var params = new ProducerParameters();
    params.setHost("example.com");
    params.setSolverFile(solver);
    params.setInstanceFile(instance);
    assertTrue(mode.isSet(params));
    try {
      var source = mode.createSource(params);
      assertTrue(source instanceof SolverSource);
    } catch (SourceException e) {
      fail("valid source couldn't be created", e);
    }

    Files.delete(solver);
    Files.delete(instance);
  }
}
