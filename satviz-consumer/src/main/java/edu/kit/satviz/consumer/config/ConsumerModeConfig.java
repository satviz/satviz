package edu.kit.satviz.consumer.config;

public abstract class ConsumerModeConfig {

  private ConsumerMode mode;

  public void setMode(ConsumerMode mode) {
    this.mode = mode;
  }

  public ConsumerMode getMode() {
    return mode;
  }
}
