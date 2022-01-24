package edu.kit.satviz.producer.source;

import edu.kit.satviz.producer.ClauseSource;
import edu.kit.satviz.producer.SourceOpeningException;
import edu.kit.satviz.sat.ClauseUpdate;

import java.util.Iterator;

public class ProofSource extends ClauseSource {

  private final Iterator<ClauseUpdate> updates;

  public ProofSource(Iterator<ClauseUpdate> updates) {
    this.updates = updates;
  }

  @Override
  public void open() throws SourceOpeningException {

  }

  @Override
  public void close() throws Exception {

  }
}
