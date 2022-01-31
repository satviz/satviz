package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

public class DratFile implements Iterable<ClauseUpdate>, AutoCloseable {

  private final Scanner scanner;
  private final ClauseParsingIterator parsingIterator;

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
