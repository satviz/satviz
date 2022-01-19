package edu.kit.satviz.consumer.config;

public class ExternalModeConfig extends ConsumerModeConfig {

  private int port;

  public void setPort(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }
}
