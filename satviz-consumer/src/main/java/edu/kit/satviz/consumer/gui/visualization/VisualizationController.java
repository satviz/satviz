package edu.kit.satviz.consumer.gui.visualization;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.Theme;
import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.gui.GuiUtils;
import edu.kit.satviz.consumer.processing.Mediator;
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
import javafx.scene.text.Font;

/**
 * This class offers functionality to control the live visualization with GUI components.
 *
 * <p>Most of the methods of this class are actually implemented in the {@link Mediator} class
 * - this class merely delegates the method calls to a given {@link Mediator} object.</p>
 */
public class VisualizationController {

  // CONSTANTS

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
  private Spinner<Integer> bufferSizeSpinner;
  @FXML
  private Spinner<Integer> windowSizeSpinner;
  @FXML
  private ColorPicker coldColorColorPicker;
  @FXML
  private ColorPicker hotColorColorPicker;
  @FXML
  private Button startOrStopRecordingButton;
  @FXML
  private Button pauseOrContinueRecordingButton;
  @FXML
  private Button pauseOrContinueVisualizationButton;
  @FXML
  private Button resetCameraButton;
  @FXML
  private Button relayoutButton;
  // This is actually meant to be a Spinner<Long>.
  // However, there is no default implementation of a LongSpinnerValueFactory.
  // Hence, a DoubleSpinnerValueFactory is used in which all values
  // are basically treated as long values.
  // Furthermore, since JavaFX-Spinners also only work with double values
  // (see processedClausesSlider), this simplifies setting one component to
  // the value of the respective other.
  @FXML
  private Spinner<Double> processedClausesSpinner;
  @FXML
  private Label totalClausesLabel;
  @FXML
  private Slider processedClausesSlider;

  // ATTRIBUTES (OTHER)

  private final Mediator mediator;
  private final ConsumerConfig config;

  private boolean recording;
  private boolean recordingPaused;
  private boolean visualizationRunning;
  // a single variable instead of two separate ones will most likely also suffice
  private boolean processedClausesSliderMousePressed;
  private boolean processedClausesSliderKeyPressed;

  private boolean initialized = false;

  private ChangeListener<String> processedClausesSpinnerLongValidationListener;


  // CONSTRUCTORS

  /**
   * Creates a new {@link VisualizationController} object with the given parameters.
   *
   * @param mediator The {@link Mediator} object to which this class delegates its method calls.
   * @param config The {@link ConsumerConfig} object containing the initial values
   *               for the configuration parameters.
   */
  public VisualizationController(Mediator mediator, ConsumerConfig config) {
    this.mediator = mediator;
    this.mediator.registerCloseAction(Platform::exit);
    this.config = config;
  }

  // METHODS (FXML)

  @FXML
  private void initialize() {
    weightFactorChoiceBox.setItems(FXCollections.observableArrayList(WeightFactor.values()));
    weightFactorChoiceBox.setValue(config.getWeightFactor());

    GuiUtils.initializeIntegerSpinner(bufferSizeSpinner,
        ConsumerConfig.MIN_BUFFER_SIZE,
        ConsumerConfig.MAX_BUFFER_SIZE,
        config.getBufferSize(),
        ConsumerConfig.STEP_AMOUNT_BUFFER_SIZE);

    GuiUtils.setOnFocusLost(bufferSizeSpinner, this::updateBufferSize);

    GuiUtils.initializeIntegerSpinner(windowSizeSpinner,
        ConsumerConfig.MIN_WINDOW_SIZE,
        ConsumerConfig.MAX_WINDOW_SIZE,
        config.getWindowSize(),
        ConsumerConfig.STEP_AMOUNT_WINDOW_SIZE);

    GuiUtils.setOnFocusLost(windowSizeSpinner, this::updateWindowSize);

    Theme theme = config.getTheme();
    coldColorColorPicker.setValue(theme.getColdColor());
    hotColorColorPicker.setValue(theme.getHotColor());

    recording = config.isRecordImmediately();
    updateRecordingDisplay();
    recordingPaused = false;
    updateRecordingPausedDisplay();
    visualizationRunning = true;
    updateVisualizationRunningDisplay();

    long totalClauses = mediator.numberOfUpdates();
    long processedClauses = mediator.currentUpdate();
    long amountToStepBy =
        (long) (ConsumerConfig.STEP_AMOUNT_FACTOR_PROCESSED_CLAUSES * totalClauses);

    processedClausesSpinnerLongValidationListener = GuiUtils.initializeLongSpinnerAsDouble(
        processedClausesSpinner,
        MIN_PROCESSED_CLAUSES,
        totalClauses,
        processedClauses,
        amountToStepBy);

    GuiUtils.setOnFocusLost(processedClausesSpinner, this::updateProcessedClausesSpinner);

    totalClausesLabel.setText(TOTAL_CLAUSES_DELIMITER + totalClauses);

    processedClausesSlider.setMin(MIN_PROCESSED_CLAUSES);
    processedClausesSlider.setMax(totalClauses);
    processedClausesSlider.setValue(processedClauses);
    // make slider move in discrete steps
    processedClausesSlider.setSnapToTicks(true);
    processedClausesSlider.setMajorTickUnit(1.0);
    processedClausesSlider.setBlockIncrement(amountToStepBy);
    processedClausesSlider.setMinorTickCount(0); // Disable minor ticks

    // set button fonts
    Font quivira = Font.loadFont(this.getClass().getResourceAsStream("Quivira.otf"), -1);
    startOrStopRecordingButton.setFont(quivira);
    pauseOrContinueRecordingButton.setFont(quivira);
    pauseOrContinueVisualizationButton.setFont(quivira);

    initialized = true;
  }

  @FXML
  private void updateWeightFactor() {
    mediator.updateWeightFactor(weightFactorChoiceBox.getValue());
  }

  @FXML
  private void updateBufferSize() {
    mediator.setClausesPerAdvance(bufferSizeSpinner.getValue());
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
  private void resetCamera() {
    mediator.resetCamera();
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
   * @see VisualizationController#VisualizationController(Mediator, ConsumerConfig)
   * @see Mediator
   */
  public void onClauseUpdate() {
    // make sure that this method can only be properly called from other threads once the
    // JavaFX thread is done initializing
    if (!initialized) {
      return;
    }

    long totalUpdates = mediator.numberOfUpdates();
    long currentUpdate = mediator.currentUpdate();
    long amountToStepBy =
        (long) (ConsumerConfig.STEP_AMOUNT_FACTOR_PROCESSED_CLAUSES * totalUpdates);

    // execute on JavaFX application thread
    Platform.runLater(() -> {
      // update spinner
      processedClausesSpinner.getEditor().textProperty().removeListener(
          processedClausesSpinnerLongValidationListener);
      processedClausesSpinnerLongValidationListener = GuiUtils.initializeLongSpinnerAsDouble(
          processedClausesSpinner,
          MIN_PROCESSED_CLAUSES,
          totalUpdates,
          currentUpdate,
          amountToStepBy);

      // update label
      totalClausesLabel.setText(TOTAL_CLAUSES_DELIMITER + totalUpdates);

      // update slider (allow slider to be moved even if clauses are currently coming in)
      if (!(processedClausesSliderMousePressed || processedClausesSliderKeyPressed)) {
        processedClausesSlider.setMax(totalUpdates);
        processedClausesSlider.setValue(currentUpdate);
        processedClausesSlider.setBlockIncrement(amountToStepBy);
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
