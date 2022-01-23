package edu.kit.satviz.consumer.config;

import java.util.Objects;

/**
 * This abstract class allows for different configuration modes
 * with different options to be set exclusively.
 *
 * @author johnnyjayjay
 */
public abstract class ConsumerModeConfig {

  private ConsumerMode mode;

  /**
   * This method allows for a specific <code>ConsumerMode</code> to be set.
   *
   * @param mode An instance of the <code>ConsumerMode</code> enum.
   */
  public void setMode(ConsumerMode mode) {
    this.mode = mode;
  }

  /**
   * This simple getter-method returns the mode of the configuration.
   *
   * @return An instance of the <code>ConsumerMode</code> enum.
   */
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
