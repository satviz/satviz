package edu.kit.satviz.producer.source;

import edu.kit.satviz.parsers.DratFile;
import edu.kit.satviz.parsers.ParsingException;
import edu.kit.satviz.producer.ClauseSource;
import edu.kit.satviz.producer.SourceException;
import edu.kit.satviz.sat.ClauseUpdate;
import java.io.IOException;
import java.util.Iterator;

public class ProofSource extends ClauseSource {

  private final DratFile proof;

  private volatile boolean stop;

  public ProofSource(DratFile proof) {
    this.proof = proof;
    this.stop = false;
  }

  @Override
  public void open() throws SourceException {
    try (proof) {
      Iterator<ClauseUpdate> iterator = proof.iterator();
      while (!stop && iterator.hasNext()) {
        clauseListener.accept(iterator.next());
      }
    } catch (ParsingException e) {
      throw new SourceException("DRAT proof parsing error", e);
    } catch (IOException e) {
      throw new SourceException("DRAT proof I/O error", e);
    }
    refutedListener.run();
  }

  @Override
  public void close() {
    stop = true;
  }
}
