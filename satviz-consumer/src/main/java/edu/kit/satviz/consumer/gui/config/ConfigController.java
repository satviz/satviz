package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import javafx.fxml.FXML;

/**
 * Abstract superclass to all controller classes for config GUI windows.
 */
public abstract class ConfigController {

  /**
   * Template method to initialize all fxml components of the window.
   */
  @FXML
  protected void initialize() {
    initializeComponents();
    setDefaultValues();
  }

  /**
   * Initialize fxml components with need for special initialization for the first time.
   */
  protected abstract void initializeComponents();

  /**
   * Set all components to their default values.
   */
  protected abstract void setDefaultValues();

  /**
   * Adopt all necessary information from the given {@link ConsumerConfig} object.
   *
   * @param config The {@link ConsumerConfig} object containing the new settings.
   */
  protected abstract void loadConsumerConfig(ConsumerConfig config);

  /**
   * Use the values currently provided by the fxml components of the window which this
   * controller controls to set the respective values of a {@link ConsumerConfig} object.
   * May leave some attributes of the {@link ConsumerConfig} object unchanged.
   */
  protected abstract void setConsumerConfigValues(ConsumerConfig config);

  /**
   * Validates that a given {@link ConsumerConfig} object contains all the necessary information
   * that this controller can (and should) provide.
   *
   * <p>
   *   This method is supposed to be called after
   *   {@link ConfigController#setConsumerConfigValues(ConsumerConfig)}.
   *   If there is a guarantee that
   *   {@link ConfigController#setConsumerConfigValues(ConsumerConfig)} always properly sets
   *   certain values, then these values don't have to be validated.
   * </p>
   *
   * @param config The {@link ConsumerConfig} object to be validated.
   * @throws ConfigArgumentException If the {@link ConsumerConfig} object doesn't contain
   *                                 all necessary information.
   */
  protected abstract void validateConsumerConfig(ConsumerConfig config)
      throws ConfigArgumentException;

}
