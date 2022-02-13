package edu.kit.satviz.consumer.gui.config;

/**
 * Thrown to indicate that an argument in a {@link edu.kit.satviz.consumer.config.ConsumerConfig}
 * is missing.
 *
 * @author Tim-17
 */
public class ConfigArgumentException extends Exception {

  /**
   * Same as {@link Exception#Exception(String)}.
   *
   * @param message The exception message.
   */
  public ConfigArgumentException(String message) {
    super(message);
  }

}
