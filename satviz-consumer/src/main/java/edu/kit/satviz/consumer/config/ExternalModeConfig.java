package edu.kit.satviz.consumer.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Objects;

/**
 * This subclass of the ConsumerModeConfig class is used, when one decides
 * to use the consumer with an external producer.
 */
@JsonTypeName("EXTERNAL")
public class ExternalModeConfig extends ConsumerModeConfig {

  public static final int MIN_PORT_NUMBER = 1;
  public static final int MAX_PORT_NUMBER = 65535;
  public static final int DEFAULT_PORT_NUMBER = 34312;

  private int port;

  /**
   * This constructor creates an instance of the ExternalModeConfig class.
   */
  public ExternalModeConfig() {
    super.setMode(ConsumerMode.EXTERNAL);
  }

  /**
   * This method sets the port, on which the clauses will be received.
   *
   * @param port The port, on which the clauses will be received.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * This simple getter-method returns the port number, on which
   * the clauses will be received.
   *
   * @return The port, on which the clauses will be received.
   */
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
