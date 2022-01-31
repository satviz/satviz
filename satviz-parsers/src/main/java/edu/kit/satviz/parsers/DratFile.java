package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

public class DratFile implements Iterable<ClauseUpdate>, AutoCloseable {

  private final InputStream in;
  private final ClauseParsingIterator parsingIterator;

  public DratFile(InputStream in) {
    this.in = in;
    Scanner scanner = new Scanner(in);
    scanner.useDelimiter("\n");
    parsingIterator = new DratParsingIterator(scanner);
  }

  @Override
  public Iterator<ClauseUpdate> iterator() {
    return parsingIterator;
  }

  @Override
  public void close() throws IOException {
    in.close();
  }

}
