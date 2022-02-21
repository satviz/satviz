package edu.kit.satviz.consumer.gui;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

/**
 * Provides functionality for fxml GUI windows.
 */
public final class GuiUtils {

  // hide public constructor
  private GuiUtils() {

  }

  /**
   * Initializes a {@code spinner} (with the initial value {@code init}) so that the user can only
   * enter valid integers within the given range (from {@code min} to {@code max}) while
   * allowing the {@code spinner} to be empty if the user is currently entering a new value
   * (i.e., has just erased an old value).
   * <p>If the user attempts to keep the {@code spinner} empty,
   * the {@code spinner} is reset to {@code init}.</p>
   *
   * @param spinner The {@code spinner} to be initialized.
   * @param min The minimum value of the {@code spinner}.
   * @param max The maximum value of the {@code spinner}.
   * @param init The initial value of the {@code spinner}.
   * @return The {@link ChangeListener} which validates that the input of the {@code spinner}
   *         is either a valid integer or empty.
   */
  public static ChangeListener<String> initializeIntegerSpinner(Spinner<Integer> spinner,
                                              int min,
                                              int max,
                                              int init) {
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
    ChangeListener<String> listener = createIntegerValidationListener(spinner, min, max);
    spinner.getEditor().textProperty().addListener(listener);

    return listener;
  }

  /**
   * Creates a new {@link ChangeListener} which validates user input of a given {@code spinner}.
   * It confirms that the given input is either a valid integer or empty.
   * If the new input is invalid it rejects it and keeps the old value.
   *
   * @param spinner The {@code spinner} whose input is supposed to be validated.
   * @param min The minimum value of the {@code spinner}.
   * @param max The maximum value of the {@code spinner}.
   * @return The validating {@link ChangeListener}.
   */
  private static ChangeListener<String> createIntegerValidationListener(Spinner<Integer> spinner,
                                                                       int min,
                                                                       int max) {
    return (observable, oldValue, newValue) -> {
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
    };
  }

  /**
   * Parses an integer representation of a color into the corresponding {@link Color} object.
   *
   * @param color The color to be parsed.
   * @return The parsed color.
   */
  public static Color intToColor(int color) {
    int red = (color >>> 16) & 0xFF;
    int green = (color >>> 8) & 0xFF;
    int blue = color & 0xFF;
    return new Color(red / 255.0, green / 255.0, blue / 255.0, 1.0);
  }

  /**
   * Parses a {@link Color} object into its corresponding integer representation.
   *
   * @param color The color to be parsed.
   * @return The parsed color.
   */
  public static int colorToInt(Color color) {
    int red = (int) Math.round(color.getRed() * 255);
    int green = (int) Math.round(color.getGreen() * 255);
    int blue = (int) Math.round(color.getBlue() * 255);
    return (red << 16) | (green << 8) | blue;
  }

}
