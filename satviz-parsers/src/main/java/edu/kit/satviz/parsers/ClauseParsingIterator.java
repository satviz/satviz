package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;

import java.util.Iterator;
import java.util.Scanner;

public abstract class ClauseParsingIterator implements Iterator<ClauseUpdate> {

  protected ClauseParsingIterator(Scanner scanner) {

  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public ClauseUpdate next() {
    return null;
  }

  protected abstract ClauseUpdate.Type readType();


}
