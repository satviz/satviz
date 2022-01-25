package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;

import java.io.InputStream;
import java.util.Iterator;

public class DimacsFile implements Iterable<ClauseUpdate> {

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
}
