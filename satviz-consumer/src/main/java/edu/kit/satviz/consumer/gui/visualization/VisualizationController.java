package edu.kit.satviz.consumer.gui.visualization;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.HeatmapColors;
import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.gui.GuiUtils;
import edu.kit.satviz.consumer.processing.Mediator;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
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
  private static final int MIN_RECEIVED_CLAUSES = 0;
  private static final String PLAY_SYMBOL = "▶";
  private static final String PAUSE_SYMBOL = "⏸";
  private static final String TOTAL_CLAUSES_RECEIVED_DELIMITER = "/ ";

  // ATTRIBUTES (FXML)

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
  @FXML
  private Spinner<Integer> receivedClausesSpinner; // TODO: convert to Spinner<Long>
  @FXML
  private Label totalClausesLabel;
  @FXML
  private Slider receivedClausesSlider;
  @FXML
  private Button relayoutButton;

  // ATTRIBUTES (OTHER)

  private final Mediator mediator;
  private final ConsumerConfig config;
  private final int variableCount;

  private boolean recording;
  private boolean recordingPaused;
  private boolean visualizationRunning;

  private ChangeListener<String> receivedClausesSpinnerIntegerValidationListener;


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

    long totalClausesReceived = mediator.numberOfUpdates();
    long currentClausesReceived = mediator.currentUpdate();

    receivedClausesSpinnerIntegerValidationListener = GuiUtils.initializeIntegerSpinner(
        receivedClausesSpinner,
        MIN_RECEIVED_CLAUSES,
        (int) totalClausesReceived,
        (int) currentClausesReceived);

    GuiUtils.setOnFocusLost(receivedClausesSpinner, this::updateReceivedClausesSpinner);

    totalClausesLabel.setText(TOTAL_CLAUSES_RECEIVED_DELIMITER + totalClausesReceived);

    receivedClausesSlider.setMin(MIN_RECEIVED_CLAUSES);
    receivedClausesSlider.setMax(totalClausesReceived);
    receivedClausesSlider.setValue(currentClausesReceived);
    // make slider move in discrete steps
    receivedClausesSlider.setSnapToTicks(true);
    receivedClausesSlider.setMajorTickUnit(1.0);
    receivedClausesSlider.setBlockIncrement(1.0);
    receivedClausesSlider.setMinorTickCount(0); // Disable minor ticks
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
  private void updateReceivedClausesSpinner() {
    long currentUpdate = receivedClausesSpinner.getValue();
    receivedClausesSlider.setValue(currentUpdate);
    mediator.seekToUpdate(currentUpdate);
  }

  @FXML
  private void updateReceivedClausesSlider() {
    long currentUpdate = (long) receivedClausesSlider.getValue();
    receivedClausesSpinner.getValueFactory().setValue((int) currentUpdate);
    mediator.seekToUpdate(currentUpdate);
  }

  // METHODS (OTHER)

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
      receivedClausesSpinner.getEditor().textProperty().removeListener(
          receivedClausesSpinnerIntegerValidationListener);
      receivedClausesSpinnerIntegerValidationListener = GuiUtils.initializeIntegerSpinner(
          receivedClausesSpinner,
          MIN_RECEIVED_CLAUSES,
          (int) totalUpdates,
          (int) currentUpdate);

      // update label
      totalClausesLabel.setText(TOTAL_CLAUSES_RECEIVED_DELIMITER + totalUpdates);

      // update slider
      receivedClausesSlider.setMax(totalUpdates);
      receivedClausesSlider.setValue(currentUpdate);
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
