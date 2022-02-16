package edu.kit.satviz.producer.source;

import edu.kit.satviz.parsers.DratFile;
import edu.kit.satviz.parsers.ParsingException;
import edu.kit.satviz.producer.ClauseSource;
import edu.kit.satviz.producer.SourceException;
import edu.kit.satviz.sat.ClauseUpdate;
import java.io.IOException;
import java.util.Iterator;

/**
 * An implementation of {@link ClauseSource} representing a DRAT proof that simply emits its
 * contained clauses.
 */
public class ProofSource extends ClauseSource {

  private final DratFile proof;

  private volatile boolean stop;

  /**
   * Creates a source from a DRAT file. The file must not have been closed.
   *
   * @param proof a {@code DratFile} that this source will read
   */
  public ProofSource(DratFile proof) {
    this.proof = proof;
    this.stop = false;
  }

  /**
   * "Opens" the proof source, i.e. emits all the clause updates
   * in the underlying proof sequentially.
   *
   * <p>When done, the function set by {@link #whenRefuted(Runnable)} will be called if the source
   * was not {@link #close() closed} prematurely.
   *
   * <p>This method closes the underlying {@code DratFile}.
   *
   * @throws SourceException if one of the following is the case:
   *                         <ul>
   *                            <li>The underlying proof file is not valid DRAT</li>
   *                            <li>An {@code IOException} occurs</li>
   *                         </ul>
   */
  @Override
  public void open() throws SourceException {
    try (proof) {
      Iterator<ClauseUpdate> iterator = proof.iterator();
      while (!stop && iterator.hasNext()) {
        clauseListener.accept(iterator.next());
      }
    } catch (ParsingException e) {
      throw new SourceException("DRAT proof parsing error", e);
    }

    if (!stop) {
      refutedListener.run();
    }
  }

  @Override
  public void close() {
    stop = true;
  }
}
