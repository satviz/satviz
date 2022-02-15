package edu.kit.satviz.common;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PathArgumentTypeTest {

  private ArgumentParser parser;

  @BeforeEach
  void setUp() {
    PathArgumentType type = PathArgumentType.get();
    parser = ArgumentParsers.newFor("test").build();
    parser.addArgument("--foo").type(type);
  }

  @Test
  void testInvalidPath() {
    String[] args = {"--foo", "\0/:*?|<>"};
    assertThrows(ArgumentParserException.class, () -> parser.parseArgs(args));
  }

  @Test
  void testRelativePath() throws ArgumentParserException {
    String[] args = {"--foo", "foo/bar/baz.txt"};
    Namespace ns = parser.parseArgs(args);
    Path dest = ns.get("foo");
    assertEquals(Paths.get("foo/bar/baz.txt"), dest);
  }

}
