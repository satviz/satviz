package edu.kit.satviz.producer.source;

import edu.kit.satviz.parsers.DratFile;
import edu.kit.satviz.producer.ClauseSource;
import edu.kit.satviz.producer.SourceException;

public class ProofSource extends ClauseSource {

  private final DratFile drat;

  public ProofSource(DratFile drat) {
    this.drat = drat;
  }

  @Override
  public void open() throws SourceException {

  }

  @Override
  public void close() throws Exception {

  }
}
