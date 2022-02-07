package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;

public abstract class ConfigController {

  protected abstract ConsumerConfig createConsumerConfig() throws ConfigArgumentException;

}
