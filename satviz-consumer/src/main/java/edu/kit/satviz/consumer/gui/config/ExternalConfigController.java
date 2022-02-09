package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ConsumerModeConfig;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;

public class ExternalConfigController extends ModeConfigController {

  // ATTRIBUTES (FXML)

  @FXML
  private Spinner<Integer> portSpinner;


  // METHODS (FXML)

  @Override
  protected void initializeComponents() {
    initializeIntegerSpinner(portSpinner,
        ExternalModeConfig.MIN_PORT_NUMBER,
        ExternalModeConfig.MAX_PORT_NUMBER,
        ExternalModeConfig.DEFAULT_PORT_NUMBER);
  }

  // METHODS (OTHER)

  @Override
  protected void setDefaultValues() {
    portSpinner.getValueFactory().setValue(ExternalModeConfig.DEFAULT_PORT_NUMBER);
  }

  @Override
  protected ConsumerConfig createConsumerConfig() throws ConfigArgumentException {
    ExternalModeConfig modeConfig = new ExternalModeConfig();
    modeConfig.setPort(portSpinner.getValue());

    ConsumerConfig config = new ConsumerConfig();
    config.setModeConfig(modeConfig);

    return config;
  }

  @Override
  protected void loadSettings(ConsumerModeConfig config) {
    ExternalModeConfig externalModeConfig = (ExternalModeConfig) config;

    portSpinner.getValueFactory().setValue(externalModeConfig.getPort());
  }


}
