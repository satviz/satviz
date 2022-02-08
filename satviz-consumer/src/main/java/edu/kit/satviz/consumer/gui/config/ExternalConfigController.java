package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

public class ExternalConfigController extends ConfigController {

  @FXML
  private Spinner<Integer> portSpinner;

  @FXML
  private void initialize() {
    initializeIntegerSpinner(portSpinner,
        ExternalModeConfig.MIN_PORT_NUMBER,
        ExternalModeConfig.MAX_PORT_NUMBER,
        ExternalModeConfig.DEFAULT_PORT_NUMBER);
  }

  @Override
  protected ConsumerConfig createConsumerConfig() throws ConfigArgumentException {
    ExternalModeConfig modeConfig = new ExternalModeConfig();
    modeConfig.setPort(portSpinner.getValue());

    ConsumerConfig config = new ConsumerConfig();
    config.setModeConfig(modeConfig);

    return config;
  }

}
