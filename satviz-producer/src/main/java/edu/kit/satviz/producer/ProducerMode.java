package edu.kit.satviz.producer;

import edu.kit.satviz.producer.cli.ProducerParameters;

public interface ProducerMode {

  boolean isSet(ProducerParameters parameters);

  ClauseSource createSource(ProducerParameters parameters);

}
