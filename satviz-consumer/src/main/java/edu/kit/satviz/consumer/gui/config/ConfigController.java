package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

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

  protected abstract ConsumerConfig createConsumerConfig() throws ConfigArgumentException;

  protected void initializeIntegerSpinner(Spinner<Integer> spinner, int min, int max, int init) {
    SpinnerValueFactory<Integer> spinnerValueFactory
        = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, init);

    // prevent exception when [value is null & (enter/arrow up/arrow down) is pressed]
    spinnerValueFactory.setConverter(new StringConverter<>() {
      @Override
      public String toString(Integer object) {
        return object.toString();
      }

      @Override
      public Integer fromString(String string) {
        try {
          return Integer.parseInt(string);
        } catch (NumberFormatException e) {
          spinner.getEditor().setText("" + init);
          return init;
        }
      }
    });

    spinner.setValueFactory(spinnerValueFactory);

    // prevent user from entering invalid characters
    spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.equals("")) { // allow empty spinner
        // check if newValue is an integer
        Integer value = null;
        try {
          value = Integer.parseInt(newValue);
        } catch (NumberFormatException e) {
          spinner.getEditor().setText(oldValue);
        }

        // check if newValue is in bounds
        if (value != null && (value < min || value > max)) {
          spinner.getEditor().setText(oldValue);
        }
      }
    });
  }

}
