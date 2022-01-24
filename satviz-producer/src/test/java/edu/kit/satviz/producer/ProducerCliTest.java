package edu.kit.satviz.producer;

import edu.kit.satviz.producer.cli.ProducerCli;
import edu.kit.satviz.producer.cli.ProducerParameters;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ProducerCliTest {

  @Test
  void test_parseArgs_invalidPort() {
    String[] args = {"-H", "example.com", "-P", "foobar"};
    assertThrows(ArgumentParserException.class, () -> ProducerCli.parseArgs(args));
  }

  @Test
  void test_parseArgs_missingHost() {
    String[] args = {};
    assertThrows(ArgumentParserException.class, () -> ProducerCli.parseArgs(args));
  }

  @Test
  void test_parseArgs_validArgs() {
    var expected = new ProducerParameters();
    expected.setHost("example.com");
    expected.setPort(1234);
    expected.setProofFile(Paths.get("foo/bar.drat"));
    expected.setNoWait(true);
    String[] args = {"-H", "example.com", "-P", "1234", "-p", "foo/bar.drat", "--no-wait"};
    try {
      var params = ProducerCli.parseArgs(args);
      assertEquals(expected, params);
    } catch (ArgumentParserException e) {
      fail(e);
    }
  }

  @Test
  void test_parseArgs_defaults() {
    var expected = new ProducerParameters();
    expected.setPort(34312);
    expected.setHost("example.com");
    expected.setNoWait(false);
    expected.setSolverFile(Paths.get("foo/bar.so"));
    expected.setInstanceFile(Paths.get("instance.cnf"));
    String[] args = {"-H", "example.com", "-s", "foo/bar.so", "-i", "instance.cnf"};
    try {
      var params = ProducerCli.parseArgs(args);
      assertEquals(expected, params);
    } catch (ArgumentParserException e) {
      fail(e);
    }
  }

}
