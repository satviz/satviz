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
   * Create a {@link ConsumerConfig} object containing all the information that is currently
   * provided by the fxml components of the window which this controller controls.
   * May result in some attributes of the {@link ConsumerConfig} object being null.
   *
   * @return The {@link ConsumerConfig} object.
   */
  protected abstract ConsumerConfig saveConsumerConfig();

  /**
   * Validates that a given {@link ConsumerConfig} object contains all the necessary information
   * to start the visualization.
   * This method is only to be called after {@link ConfigController#saveConsumerConfig()}.
   *
   * @param config The {@link ConsumerConfig} object to be validated.
   * @throws ConfigArgumentException If the {@link ConsumerConfig} object doesn't contain
   * all necessary information.
   */
  protected abstract void validateConsumerConfig(ConsumerConfig config)
      throws ConfigArgumentException;

}
