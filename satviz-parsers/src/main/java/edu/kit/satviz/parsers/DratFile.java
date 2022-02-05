package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

/**
 * This class is used to parse an <code>InputStream</code> that complies with the DRAT format.
 */
public class DratFile implements Iterable<ClauseUpdate>, AutoCloseable {

  private final Scanner scanner;
  private final ClauseParsingIterator parsingIterator;

  /**
   * This constructor creates an instance of the <code>DratFile</code> class.
   *
   * @param in An instance of the <code>InputStream</code> class.
   */
  public DratFile(InputStream in) {
    scanner = new Scanner(in);
    parsingIterator = new DratParsingIterator(scanner);
  }

  @Override
  public Iterator<ClauseUpdate> iterator() {
    return parsingIterator;
  }

  @Override
  public void close() {
    scanner.close();
  }

}
