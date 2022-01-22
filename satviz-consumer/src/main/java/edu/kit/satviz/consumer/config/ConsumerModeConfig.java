package edu.kit.satviz.consumer.config;

import java.util.Objects;

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
    ConsumerModeConfig that = (ConsumerModeConfig) o;
    return mode == that.mode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(mode);
  }
}
