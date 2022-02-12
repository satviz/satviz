package edu.kit.satviz.consumer.gui;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * Provides functionality for fxml GUI windows.
 */
public interface GuiUtils {

  /**
   * Initializes a {@link Spinner} so that the user can only enter valid integers while allowing
   * it to be empty if the user is currently entering a new value
   * (i.e., has just erased an old value).
   *
   * @param spinner The {@link Spinner} to be initialized.
   * @param min The minimum value of this {@link Spinner}.
   * @param max The maximum value of this {@link Spinner}.
   * @param init The initial value of this {@link Spinner}.
   */
  static void initializeIntegerSpinner(Spinner<Integer> spinner, int min, int max, int init) {
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
