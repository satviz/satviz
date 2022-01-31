package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class DratFile implements Iterable<ClauseUpdate>, AutoCloseable {

  private final InputStream in;
  private final ClauseParsingIterator parsingIterator;

  public DratFile(InputStream in) {
    this.in = in;
    this.parsingIterator = new DimacsParsingIterator(new Scanner(in));
  }

  @Override
  public Iterator<ClauseUpdate> iterator() {
    return this.parsingIterator;
  }

  @Override
  public void close() throws IOException {
    this.in.close();
  }

}
