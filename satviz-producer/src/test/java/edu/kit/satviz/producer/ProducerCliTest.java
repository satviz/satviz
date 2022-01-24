package edu.kit.satviz.producer;

import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.network.ProducerConnection;
import edu.kit.satviz.producer.cli.ProducerCli;
import edu.kit.satviz.producer.cli.ProducerParameters;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.junit.jupiter.api.Assertions;
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
    expected.setSolverFile(Paths.get("foo/bar.so"));
    expected.setNoWait(true);
    String[] args = {"-H", "example.com", "-P", "1234", "-s", "foo/bar.so", "--no-wait"};
    try {
      var params = ProducerCli.parseArgs(args);
      assertEquals(expected, params);
    } catch (ArgumentParserException e) {
      fail(e);
    }
  }

}
