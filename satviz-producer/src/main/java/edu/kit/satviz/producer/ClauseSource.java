package edu.kit.satviz.producer;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.SatAssignment;

import java.util.function.Consumer;

public abstract class ClauseSource implements AutoCloseable {

  public abstract void open() throws SourceOpeningException;

  public void subscribe(Consumer<Clause> listener) {

  }

  public void whenClosed(Runnable action) {

  }

  public void whenDone(Consumer<SatAssignment> action) {

  }

  protected void notifyListeners(Clause clause) {

  }

}
