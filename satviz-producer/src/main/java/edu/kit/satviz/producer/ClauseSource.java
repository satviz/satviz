package edu.kit.satviz.producer;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An abstraction over the different ways SAT clause updates can be produced.
 *
 * <p>Listeners can be registered to be notified of new clause updates or when the stream of clauses
 * "solves" or "refutes" an instance. These terms are generalised for all types of clause sources
 * and their use depends on the implementation.<br>
 * If, for example, a source implementation lacks the notion of producing a "solution", then it may
 * simply always call the "refuted" listener when it is drained.
 */
public abstract class ClauseSource implements AutoCloseable {

  protected Consumer<? super ClauseUpdate> clauseListener;
  protected Consumer<? super SatAssignment> solvedListener;
  protected Runnable refutedListener;

  /**
   * Default constructor, initialises all listeners as no-ops.
   */
  protected ClauseSource() {
    clauseListener = c -> {};
    solvedListener = a -> {};
    refutedListener = () -> {};
  }

  /**
   * Opens this source, making it start producing clauses.
   *
   * <p>This method is <strong>blocking</strong> until the source has been drained or a
   * {@link #close()} call has come through.
   *
   * <p>This method may only be called <strong>once</strong> per source.
   *
   * @throws SourceException If there is an exception while opening the source or
   *                         producing the clauses
   */
  public abstract void open() throws SourceException;

  /**
   * Sets the clause update listener for this source.
   *
   * <p>The listener will be called from the same thread {@link #open()} was called from
   * whenever this source produces a new clause update.
   *
   * @param listener a {@code Consumer} that accepts {@code ClauseUpdate}s.
   */
  public void subscribe(Consumer<? super ClauseUpdate> listener) {
    clauseListener = Objects.requireNonNull(listener);
  }

  /**
   * Sets the "refuted" listener for this source.
   *
   * <p>The listener will be called from the same thread {@link #open()} was called from under
   * implementation-specific conditions when the source has been drained.
   *
   * @param action a {@code Runnable}.
   */
  public void whenRefuted(Runnable action) {
    refutedListener = Objects.requireNonNull(action);
  }

  /**
   * Sets the "solved" listener for this source.
   *
   * <p>The listener will be called from the same thread {@link #open()} was called from under
   * implementation-specific conditions when the source has been drained.
   *
   * @param action a {@code Consumer} that accepts {@code SatAssignment}s.
   */
  public void whenSolved(Consumer<? super SatAssignment> action) {
    solvedListener = Objects.requireNonNull(action);
  }

  /**
   * Signals to the source that it should be closed, i.e. stop emitting clause updates.
   *
   * <p>This method can - and, practically, must - be called from a
   * different thread than {@link #open()} (because {@code open} blocks until the source
   * is done emitting clause updates, thus closed).
   *
   * <p>Due to the blocking semantics of {@link #open()} mentioned above as well as implementation
   * details of the different sources, there is no guarantee that calling this method will result
   * in an immediate end of clause emission.
   */
  public abstract void close();

}
