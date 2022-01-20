package edu.kit.satviz.consumer.config;

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
    return super.equals(o)
            && port == ((ExternalModeConfig) o).port;
  }

  @Override
  public int hashCode() {
    // TODO
    return super.hashCode();
  }

}
