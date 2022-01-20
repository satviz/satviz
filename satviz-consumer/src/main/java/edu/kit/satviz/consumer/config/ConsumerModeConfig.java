package edu.kit.satviz.consumer.config;

public abstract class ConsumerModeConfig {

  private ConsumerMode mode;

  public void setMode(ConsumerMode mode) {
    this.mode = mode;
  }

  public ConsumerMode getMode() {
    return mode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsumerModeConfig modeConfig = (ConsumerModeConfig) o;
    return mode.equals(modeConfig.mode);
  }

  @Override
  public int hashCode() {
    // TODO
    return super.hashCode();
  }

}
