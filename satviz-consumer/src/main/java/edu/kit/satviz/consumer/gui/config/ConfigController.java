package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;

public abstract class ConfigController {

  private ConsumerConfig config;

  protected abstract void run() throws ConfigArgumentException;

  protected void setConsumerConfig(ConsumerConfig config) {
    this.config = config;
  }

  public ConsumerConfig getConsumerConfig() {
    return this.config;
  }

}
