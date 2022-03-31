package edu.kit.satviz.consumer.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;

/**
 * This abstract class allows for different configuration modes
 * with different options to be set exclusively.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "mode")
@JsonSubTypes({
    @JsonSubTypes.Type(value = EmbeddedModeConfig.class, name = "EMBEDDED"),
    @JsonSubTypes.Type(value = ExternalModeConfig.class, name = "EXTERNAL")
})
public abstract class ConsumerModeConfig {

  private ConsumerMode mode;

  @JsonIgnore
  /**
   * This method allows for a specific <code>ConsumerMode</code> to be set.
   *
   * @param mode An instance of the <code>ConsumerMode</code> enum.
   */
  protected void setMode(ConsumerMode mode) {
    this.mode = mode;
  }

  @JsonIgnore
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
