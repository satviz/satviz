package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import javafx.fxml.FXML;

public abstract class ConfigController {

  /**
   * template method.
   */
  @FXML
  protected void initialize() {
    initializeComponents();
    setDefaultValues();
  }

  protected abstract void initializeComponents();

  protected abstract void setDefaultValues();

  protected abstract void loadConsumerConfig(ConsumerConfig config);

  protected abstract ConsumerConfig saveConsumerConfig();

  protected abstract void validateConsumerConfig(ConsumerConfig config)
      throws ConfigArgumentException;

}
