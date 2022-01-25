package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;

import java.io.InputStream;
import java.util.Iterator;

public class DratFile implements Iterable<ClauseUpdate> {

  public DratFile(InputStream in) {

  }

  @Override
  public Iterator<ClauseUpdate> iterator() {
    return null;
  }
}
