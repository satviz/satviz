package edu.kit.satviz.common;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;

/**
 * An {@code ArgumentType} implementation that parses its input to a {@link java.nio.file.Path}
 * object.<br>
 * This class is a singleton.
 *
 * @see #get()
 */
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
      throw new ArgumentParserException(value + " is not a valid path: "
          + e.getMessage(), e, parser);
    }
  }

  /**
   * Gets the singleton instance of this ArgumentType.
   *
   * @return the {@code PathArgumentType} instance.
   */
  public static PathArgumentType get() {
    return INSTANCE;
  }
}
