package edu.kit.satviz.consumer.config;

import java.util.Objects;

public class ExternalModeConfig extends ConsumerModeConfig {

  private int port;

  public void setPort(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }
    ExternalModeConfig that = (ExternalModeConfig) o;
    return port == that.port;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), port);
  }
}
