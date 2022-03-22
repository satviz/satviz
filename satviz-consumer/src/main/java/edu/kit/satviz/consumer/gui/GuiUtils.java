package edu.kit.satviz.consumer.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Provides functionality for fxml GUI windows.
 */
public final class GuiUtils {

  // CONSTANTS

  public static final Object CONFIG_MONITOR = new Object();

  /**
   * An extension filter for file choosing dialogues that shows all files.
   */
  public static final FileChooser.ExtensionFilter ALL_FILES
      = new FileChooser.ExtensionFilter("All Files", "*.*");

  // ATTRIBUTES

  private static volatile boolean javaFxLaunched = false;


  // CONSTRUCTORS

  // hide public constructor
  private GuiUtils() {

  }

  // METHODS

  /**
   * Custom launch method for {@link Application#launch(Class, String...)} that can be called
   * multiple times and that doesn't block (unlike {@link Application#launch(Class, String...)}).
   *
   * <p>
   *   Copied from <a href=https://stackoverflow.com/questions/24320014/how-to-call-launch-more-than-once-in-java/61771424#61771424>StackOverflow</a>.
   *   Only one line was slightly modified.
   * </p>
   *
   * @param applicationClass The class to be launched
   *
   * @see Application#launch(Class, String...)
   */
  public static void launch(Class<? extends Application> applicationClass) {
    if (!javaFxLaunched) { // First time
      Platform.setImplicitExit(false);
      new Thread(() -> Application.launch(applicationClass)).start();
      javaFxLaunched = true;
    } else { // Next times
      Platform.runLater(() -> {
        try {
          // the following line was updated in order
          // not to use the deprecated newInstance() method anymore
          Application application = applicationClass.getDeclaredConstructor().newInstance();
          Stage primaryStage = new Stage();
          application.start(primaryStage);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }
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
                                              int init,
                                              int amountToStepBy) {
    SpinnerValueFactory<Integer> spinnerValueFactory
        = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, init, amountToStepBy);

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

    // this must be returned so that it can be removed again later when the spinner is updated
    // see: VisualizationController#onClauseUpdate
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
   * Initializes a {@code spinner} (with the initial value {@code init}) so that the user can only
   * enter valid long integers within the given range (from {@code min} to {@code max}) while
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
   *         is either a valid long integer or empty.
   */
  public static ChangeListener<String> initializeLongSpinnerAsDouble(Spinner<Double> spinner,
                                                                long min,
                                                                long max,
                                                                long init,
                                                                long amountToStepBy) {
    SpinnerValueFactory<Double> spinnerValueFactory
            = new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, init, amountToStepBy);

    // prevent exception when [value is null & (enter/arrow up/arrow down) is pressed]
    spinnerValueFactory.setConverter(new StringConverter<>() {
      @Override
      public String toString(Double object) {
        return object == null ? "" : String.valueOf(object.longValue());
      }

      @Override
      public Double fromString(String string) {
        try {
          return (double) Long.parseLong(string);
        } catch (NumberFormatException e) {
          spinner.getEditor().setText("" + init);
          return (double) init;
        }
      }
    });

    spinner.setValueFactory(spinnerValueFactory);

    // prevent user from entering invalid characters
    ChangeListener<String> listener = createLongValidationListenerAsDouble(spinner, min, max);
    spinner.getEditor().textProperty().addListener(listener);

    // this must be returned so that it can be removed again later when the spinner is updated
    // see: VisualizationController#onClauseUpdate
    return listener;
  }

  /**
   * Creates a new {@link ChangeListener} which validates user input of a given {@code spinner}.
   * It confirms that the given input is either a valid long integer or empty.
   * If the new input is invalid it rejects it and keeps the old value.
   *
   * @param spinner The {@code spinner} whose input is supposed to be validated.
   * @param min The minimum value of the {@code spinner}.
   * @param max The maximum value of the {@code spinner}.
   * @return The validating {@link ChangeListener}.
   */
  private static ChangeListener<String> createLongValidationListenerAsDouble(
      Spinner<Double> spinner,
      long min,
      long max) {
    return (observable, oldValue, newValue) -> {
      if (!newValue.equals("")) { // allow empty spinner
        // check if newValue is a long integer
        Long value = null;
        try {
          value = Long.parseLong(newValue);
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
   * Runs the {@code action} whenever the {@code spinner} loses focus.
   *
   * @param spinner The spinner to induce {@code action} to run
   * @param action The action to be run
   * @param <T> The type of values the {@code spinner} holds (irrelevant for this method)
   */
  public static <T> void setOnFocusLost(Spinner<T> spinner, Runnable action) {
    spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (Boolean.FALSE.equals(newValue)) { // spinner lost focus
        action.run();
      }
    });
  }

}
