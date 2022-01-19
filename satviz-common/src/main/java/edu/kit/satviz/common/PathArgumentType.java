package edu.kit.satviz.common;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;

public final class PathArgumentType implements ArgumentType<Path> {

  private static final PathArgumentType INSTANCE = new PathArgumentType();

  private PathArgumentType() {

  }

  @Override
  public Path convert(ArgumentParser parser, Argument arg, String value)
      throws ArgumentParserException {
    try {
      return Paths.get(value);
    } catch (InvalidPathException e) {
      throw new ArgumentParserException(e.getMessage(), e, parser);
    }
  }

  public static PathArgumentType get() {
    return INSTANCE;
  }
}
