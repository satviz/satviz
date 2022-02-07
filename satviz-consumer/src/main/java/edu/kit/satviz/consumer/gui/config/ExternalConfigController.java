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
    SpinnerValueFactory<Integer> portSpinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
        ExternalModeConfig.MIN_PORT_NUMBER, ExternalModeConfig.MAX_PORT_NUMBER, ExternalModeConfig.DEFAULT_PORT_NUMBER);

    // catch exception when (value is null & (enter/arrow up/arrow down) is pressed)
    portSpinnerValueFactory.setConverter(new StringConverter<>() {
      @Override
      public String toString(Integer object) {
        return object.toString();
      }

      @Override
      public Integer fromString(String string) {
        try {
          return Integer.parseInt(string);
        } catch (NumberFormatException e) {
          portSpinner.getEditor().setText("" + ExternalModeConfig.DEFAULT_PORT_NUMBER);
          return ExternalModeConfig.DEFAULT_PORT_NUMBER;
        }
      }
    });

    portSpinner.setValueFactory(portSpinnerValueFactory);

    portSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.equals("")) {
        try {
          Integer.parseInt(newValue);
        } catch (NumberFormatException e) {
          portSpinner.getEditor().setText(oldValue);
        }
      }
    });
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
