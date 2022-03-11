package edu.kit.satviz.consumer.gui.visualization;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.HeatmapColors;
import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.gui.GuiUtils;
import edu.kit.satviz.consumer.processing.Mediator;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.paint.Color;

/**
 * This class offers functionality to control the live visualization with GUI components.
 *
 * <p>Most of the methods of this class are actually implemented in the {@link Mediator} class
 * - this class merely delegates the method calls to a given {@link Mediator} object.</p>
 */
public class VisualizationController {

  // CONSTANTS

  private static final int MIN_HIGHLIGHT_VARIABLE = 1;
  private static final int DEFAULT_HIGHLIGHT_VARIABLE = 1;
  private static final int MIN_PROCESSED_CLAUSES = 0;
  private static final String PLAY_SYMBOL = "▶";
  private static final String PAUSE_SYMBOL = "⏸";
  private static final String TOTAL_CLAUSES_DELIMITER = "/ ";

  // ATTRIBUTES (FXML)

  @FXML
  private TitledPane windowPane;
  @FXML
  private ChoiceBox<WeightFactor> weightFactorChoiceBox;
  @FXML
  private Spinner<Integer> windowSizeSpinner;
  @FXML
  private ColorPicker coldColorColorPicker;
  @FXML
  private ColorPicker hotColorColorPicker;
  @FXML
  private Spinner<Integer> highlightVariableSpinner;
  @FXML
  private Button clearHighlightVariableButton;
  @FXML
  private Button screenshotButton;
  @FXML
  private Button openScreenshotFolderButton;
  @FXML
  private Button startOrStopRecordingButton;
  @FXML
  private Button pauseOrContinueRecordingButton;
  @FXML
  private Button pauseOrContinueVisualizationButton;
  // This is actually meant to be a Spinner<Long>.
  // However, there is no default implementation of a LongSpinnerValueFactory.
  // Hence, a DoubleSpinnerValueFactory is used in which all values
  // are basically treated as long values.
  @FXML
  private Spinner<Double> processedClausesSpinner;
  @FXML
  private Label totalClausesLabel;
  @FXML
  private Slider processedClausesSlider;
  @FXML
  private Button relayoutButton;

  // ATTRIBUTES (OTHER)

  private final Mediator mediator;
  private final ConsumerConfig config;
  private final int variableCount;

  private boolean recording;
  private boolean recordingPaused;
  private boolean visualizationRunning;
  // a single variable instead of two separate ones will most likely also suffice
  private boolean processedClausesSliderMousePressed;
  private boolean processedClausesSliderKeyPressed;

  private ChangeListener<String> processedClausesSpinnerLongValidationListener;


  // CONSTRUCTORS

  /**
   * Creates a new {@link VisualizationController} object with the given parameters.
   *
   * @param mediator The {@link Mediator} object to which this class delegates its method calls.
   * @param config The {@link ConsumerConfig} object containing the initial values
   *               for the configuration parameters.
   * @param variableCount The number of variables of the SAT instance
   *                      that is supposed to be visualized.
   */
  public VisualizationController(Mediator mediator, ConsumerConfig config, int variableCount) {
    this.variableCount = variableCount;
    this.mediator = mediator;
    this.mediator.registerCloseAction(Platform::exit);
    this.config = config;
  }

  // METHODS (FXML)

  @FXML
  private void initialize() {
    weightFactorChoiceBox.setItems(FXCollections.observableArrayList(WeightFactor.values()));
    weightFactorChoiceBox.setValue(config.getWeightFactor());

    GuiUtils.initializeIntegerSpinner(windowSizeSpinner,
        ConsumerConfig.MIN_WINDOW_SIZE,
        ConsumerConfig.MAX_WINDOW_SIZE,
        config.getWindowSize());

    GuiUtils.setOnFocusLost(windowSizeSpinner, this::updateWindowSize);

    HeatmapColors colors = config.getHeatmapColors();
    coldColorColorPicker.setValue(GuiUtils.intToColor(colors.getFromColor()));
    hotColorColorPicker.setValue(GuiUtils.intToColor(colors.getToColor()));

    GuiUtils.initializeIntegerSpinner(highlightVariableSpinner,
        MIN_HIGHLIGHT_VARIABLE,
        variableCount,
        DEFAULT_HIGHLIGHT_VARIABLE);

    GuiUtils.setOnFocusLost(highlightVariableSpinner, this::highlightVariable);

    recording = config.isRecordImmediately();
    updateRecordingDisplay();
    recordingPaused = false;
    updateRecordingPausedDisplay();
    visualizationRunning = true;
    updateVisualizationRunningDisplay();

    long totalClauses = mediator.numberOfUpdates();
    long processedClauses = mediator.currentUpdate();

    processedClausesSpinnerLongValidationListener = GuiUtils.initializeLongSpinnerAsDouble(
        processedClausesSpinner,
        MIN_PROCESSED_CLAUSES,
        (int) totalClauses,
        (int) processedClauses);

    GuiUtils.setOnFocusLost(processedClausesSpinner, this::updateProcessedClausesSpinner);

    totalClausesLabel.setText(TOTAL_CLAUSES_DELIMITER + totalClauses);

    processedClausesSlider.setMin(MIN_PROCESSED_CLAUSES);
    processedClausesSlider.setMax(totalClauses);
    processedClausesSlider.setValue(processedClauses);
    // make slider move in discrete steps
    processedClausesSlider.setSnapToTicks(true);
    processedClausesSlider.setMajorTickUnit(1.0);
    processedClausesSlider.setBlockIncrement(1.0);
    processedClausesSlider.setMinorTickCount(0); // Disable minor ticks
  }

  @FXML
  private void updateWeightFactor() {
    mediator.updateWeightFactor(weightFactorChoiceBox.getValue());
  }

  @FXML
  private void updateWindowSize() {
    mediator.updateWindowSize(windowSizeSpinner.getValue());
  }

  @FXML
  private void updateHeatmapColdColor() {
    mediator.updateHeatmapColdColor(coldColorColorPicker.getValue());
  }

  @FXML
  private void updateHeatmapHotColor() {
    mediator.updateHeatmapHotColor(hotColorColorPicker.getValue());
  }

  @FXML
  private void highlightVariable() {
    mediator.highlightVariable(highlightVariableSpinner.getValue());
  }

  @FXML
  private void clearHighlightVariable() {
    mediator.clearHighlightVariable();
  }

  @FXML
  private void screenshot() {
    mediator.screenshot();
  }

  @FXML
  private void openScreenshotFolder() {
    try {
      Desktop.getDesktop().open(new File(ConsumerConfig.DEFAULT_SCREENSHOT_FOLDER));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void startOrStopRecording() {
    mediator.startOrStopRecording();
    recording = !recording;
    updateRecordingDisplay();
  }

  @FXML
  private void pauseOrContinueRecording() {
    mediator.pauseOrContinueRecording();
    recordingPaused = !recordingPaused;
    updateRecordingPausedDisplay();
  }

  @FXML
  private void pauseOrContinueVisualization() {
    mediator.pauseOrContinueVisualization();
    visualizationRunning = !visualizationRunning;
    updateVisualizationRunningDisplay();
  }

  @FXML
  private void relayout() {
    mediator.relayout();
  }

  @FXML
  private void updateProcessedClausesSpinner() {
    double currentUpdate = processedClausesSpinner.getValue();
    processedClausesSlider.setValue(currentUpdate);
    mediator.seekToUpdate((long) currentUpdate);
  }

  @FXML
  private void processedClausesSliderOnMousePressed() {
    processedClausesSliderMousePressed = true;
  }

  // this is constantly firing if key is held down...
  @FXML
  private void processedClausesSliderOnKeyPressed() {
    processedClausesSliderKeyPressed = true;
  }

  @FXML
  private void processedClausesSliderOnMouseReleased() {
    processedClausesSliderMousePressed = false;
    updateProcessedClausesSlider();
  }

  @FXML
  private void processedClausesSliderOnKeyReleased() {
    processedClausesSliderKeyPressed = false;
    updateProcessedClausesSlider();
  }

  // METHODS (OTHER)

  /**
   * Closes the application.
   */
  public void quit() {
    ForkJoinPool.commonPool().execute(() -> {
      try {
        mediator.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private void updateProcessedClausesSlider() {
    double currentUpdate = processedClausesSlider.getValue();
    processedClausesSpinner.getValueFactory().setValue(currentUpdate);
    mediator.seekToUpdate((long) currentUpdate);
  }

  /**
   * A callback method to update the {@link Spinner} and {@link Slider} of the GUI
   * for "time" controls. It is supposed to be called whenever the {@link Mediator} object
   * of this class receives or processes new clauses.
   *
   * @see VisualizationController#VisualizationController(Mediator, ConsumerConfig, int)
   * @see Mediator
   */
  public void onClauseUpdate() {
    long totalUpdates = mediator.numberOfUpdates();
    long currentUpdate = mediator.currentUpdate();

    // execute on JavaFX application thread
    Platform.runLater(() -> {
      // update spinner
      processedClausesSpinner.getEditor().textProperty().removeListener(
          processedClausesSpinnerLongValidationListener);
      processedClausesSpinnerLongValidationListener = GuiUtils.initializeLongSpinnerAsDouble(
          processedClausesSpinner,
          MIN_PROCESSED_CLAUSES,
          (int) totalUpdates,
          (int) currentUpdate);

      // update label
      totalClausesLabel.setText(TOTAL_CLAUSES_DELIMITER + totalUpdates);

      // update slider (allow slider to be moved even if clauses are currently coming in)
      if (!(processedClausesSliderMousePressed || processedClausesSliderKeyPressed)) {
        processedClausesSlider.setMax(totalUpdates);
        processedClausesSlider.setValue(currentUpdate);
      }
    });
  }

  private void updateRecordingDisplay() {
    startOrStopRecordingButton.setTextFill(recording ? Color.RED : Color.BLACK);
    pauseOrContinueRecordingButton.setDisable(!recording);
  }

  private void updateRecordingPausedDisplay() {
    pauseOrContinueRecordingButton.setText(recordingPaused ? PLAY_SYMBOL : PAUSE_SYMBOL);
  }

  private void updateVisualizationRunningDisplay() {
    pauseOrContinueVisualizationButton.setText(visualizationRunning ? PAUSE_SYMBOL : PLAY_SYMBOL);
  }
}
