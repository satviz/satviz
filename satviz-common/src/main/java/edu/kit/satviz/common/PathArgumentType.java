package edu.kit.satviz.common;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;

import java.nio.file.Path;

public class PathArgumentType implements ArgumentType<Path> {
  @Override
  public Path convert(ArgumentParser parser, Argument arg, String value) throws ArgumentParserException {
    return null;
  }
}
