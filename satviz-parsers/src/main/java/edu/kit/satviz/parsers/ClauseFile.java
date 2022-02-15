package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.io.InputStream;
import java.util.Scanner;

/**
 * This class is used to parse an <code>InputStream</code> iteratively into
 * <code>ClauseUpdates</code> by implementing the <code>Iterable</code> interface.
 */
public abstract class ClauseFile implements Iterable<ClauseUpdate>, AutoCloseable {

  protected final Scanner scanner;

  protected ClauseFile(InputStream in) {
    scanner = new Scanner(in);
    parseHeader();
  }

  /**
   * This method parses the header of the file.
   */
  protected abstract void parseHeader();

  @Override
  public void close() {
    scanner.close();
  }

}
