package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;

public abstract class ConfigController {

  protected abstract void run();

  protected void setConsumerConfig(ConsumerConfig config) {

  }

  public ConsumerConfig getConsumerConfig() {
    return null;
  }

}
