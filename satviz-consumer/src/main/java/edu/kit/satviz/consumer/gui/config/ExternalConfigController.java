package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ExternalModeConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class ExternalConfigController extends ConfigController {

  @FXML
  private Spinner<Integer> portSpinner;

  @FXML
  private void initialize() {
    // TODO: add constants in ExternalModeConfig
    SpinnerValueFactory<Integer> portSpinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
        ExternalModeConfig.MIN_PORT_NUMBER, ExternalModeConfig.MAX_PORT_NUMBER, ExternalModeConfig.DEFAULT_PORT_NUMBER);
    portSpinnerValueFactory.valueProperty().addListener((observableValue, oldValue, newValue) -> {
      // prevent exception for value being null (allow spinner to be empty)
      if (newValue == null) {
        // do nothing
      }
      // TODO: catch exception when (value is null & enter is pressed) -> display error message ?
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
  protected void run() {

  }

}
