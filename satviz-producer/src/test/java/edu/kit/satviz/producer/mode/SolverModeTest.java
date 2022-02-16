package edu.kit.satviz.producer.mode;

import static edu.kit.satviz.producer.ResourceHelper.extractResource;
import static edu.kit.satviz.producer.SolverParams.solverParams;
import static org.junit.jupiter.api.Assertions.*;

import edu.kit.satviz.producer.ResourceHelper;
import edu.kit.satviz.producer.SourceException;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.source.SolverSource;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SolverModeTest {

  private SolverMode mode;

  @BeforeAll
  static void createTempDir() throws IOException {
    ResourceHelper.createTempDir();
  }

  @AfterAll
  static void tearDownAll() throws IOException {
    ResourceHelper.deleteTempDir();
  }

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
  void test_createSource_invalidInstance() throws IOException {
    var params = solverParams("/libcadical.so", "/instance-broken.cnf");
    assertThrows(SourceException.class, () -> mode.createSource(params));
  }

  @Test
  void test_createSource_invalidSolver() throws IOException {
    var params = solverParams("/instance.cnf", "/instance.cnf");
    assertThrows(SourceException.class, () -> mode.createSource(params));
  }

  @Test
  void test_createSource_correctType() throws IOException {
    var params = solverParams("/libcadical.so", "/instance.cnf");
    assertTrue(mode.isSet(params));
    try {
      var source = mode.createSource(params);
      assertTrue(source instanceof SolverSource);
    } catch (SourceException e) {
      fail(e);
    }
  }


}
