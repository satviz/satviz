package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import edu.kit.satviz.consumer.gui.GuiUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;

/**
 * Controls the window for the external mode configuration.
 *
 * @author Tim-17
 */
public class ExternalConfigController extends ConfigController {

  // ATTRIBUTES (FXML)

  @FXML
  private Spinner<Integer> portSpinner;


  // METHODS (OTHER)

  @Override
  protected void initializeComponents() {
    GuiUtils.initializeIntegerSpinner(portSpinner,
        ExternalModeConfig.MIN_PORT_NUMBER,
        ExternalModeConfig.MAX_PORT_NUMBER,
        ExternalModeConfig.DEFAULT_PORT_NUMBER);
  }

  @Override
  protected void setDefaultValues() {
    portSpinner.getValueFactory().setValue(ExternalModeConfig.DEFAULT_PORT_NUMBER);
  }

  @Override
  protected void loadConsumerConfig(ConsumerConfig config) {
    setDefaultValues();

    // config & config.getModeConfig() have already been checked for null by GeneralConfigController
    ExternalModeConfig externalModeConfig = (ExternalModeConfig) config.getModeConfig();

    portSpinner.getValueFactory().setValue(externalModeConfig.getPort());
  }

  @Override
  protected ConsumerConfig saveConsumerConfig() {
    ExternalModeConfig modeConfig = new ExternalModeConfig();
    modeConfig.setPort(portSpinner.getValue());

    ConsumerConfig config = new ConsumerConfig();
    config.setModeConfig(modeConfig);

    return config;
  }

  @Override
  protected void validateConsumerConfig(ConsumerConfig config) throws ConfigArgumentException {
    // nothing to validate
  }
}
