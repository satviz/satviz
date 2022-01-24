package edu.kit.satviz.producer.mode;

import edu.kit.satviz.producer.ClauseSource;
import edu.kit.satviz.producer.ProducerMode;
import edu.kit.satviz.producer.cli.ProducerParameters;

public class ProofMode implements ProducerMode {
  @Override
  public boolean isSet(ProducerParameters parameters) {
    return parameters.getProofFile() != null;
  }

  @Override
  public ClauseSource createSource(ProducerParameters parameters) {
    return null;
  }
}
