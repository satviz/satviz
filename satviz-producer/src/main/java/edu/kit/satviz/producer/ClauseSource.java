package edu.kit.satviz.producer;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class ClauseSource implements AutoCloseable {

  protected Consumer<? super ClauseUpdate> clauseListener;
  protected Consumer<? super SatAssignment> solvedListener;
  protected Runnable refutedListener;

  protected ClauseSource() {
    clauseListener = c -> {};
    solvedListener = a -> {};
    refutedListener = () -> {};
  }

  public abstract void open() throws SourceException;

  public void subscribe(Consumer<? super ClauseUpdate> listener) {
    clauseListener = Objects.requireNonNull(listener);
  }

  public void whenRefuted(Runnable action) {
    refutedListener = Objects.requireNonNull(action);
  }

  public void whenSolved(Consumer<SatAssignment> action) {
    solvedListener = Objects.requireNonNull(action);
  }

}
