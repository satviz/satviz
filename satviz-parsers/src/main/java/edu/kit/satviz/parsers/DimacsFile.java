package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class DimacsFile implements Iterable<ClauseUpdate>, AutoCloseable {

  public DimacsFile(InputStream in) {

  }

  @Override
  public Iterator<ClauseUpdate> iterator() {
    return null;
  }

  public int getVariableAmount() {
    return 0;
  }

  public int getClauseAmount() {
    return 0;
  }

  @Override
  public void close() throws IOException {

  }
}
