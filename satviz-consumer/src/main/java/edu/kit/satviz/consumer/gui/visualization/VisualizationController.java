package edu.kit.satviz.consumer.gui.visualization;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.HeatmapColors;
import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.gui.GuiUtils;
import edu.kit.satviz.consumer.processing.Mediator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;

public class VisualizationController {

  // ATTRIBUTES (FXML)

  @FXML
  private ChoiceBox<WeightFactor> weightFactorChoiceBox;
  @FXML
  private Spinner<Integer> windowSizeSpinner; // TODO: convert to Spinner<Long>
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
  private Slider receivedClausesSlider;
  @FXML
  private Button relayoutButton;

  // ATTRIBUTES (OTHER)

  private final int variableCount;
  private final Mediator mediator;
  private final ConsumerConfig config;


  // CONSTRUCTORS

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

    HeatmapColors colors = config.getHeatmapColors();
    coldColorColorPicker.setValue(GuiUtils.intToColor(colors.getFromColor()));
    hotColorColorPicker.setValue(GuiUtils.intToColor(colors.getToColor()));

    GuiUtils.initializeIntegerSpinner(highlightVariableSpinner,
        1,
        variableCount,
        1);

    // TODO: initialize the following spinner + slider properly

    /*
    GuiUtils.initializeIntegerSpinner(receivedClausesSpinner,
        0,
        Integer.MAX_VALUE,
        0);

    GuiUtils.initializeSlider(receivedClausesSlider,
        0,
        0,
        0);
     */
  }

  @FXML
  private void updateWeightFactor() {

  }

  @FXML
  private void updateWindowSize() {

  }

  @FXML
  private void updateHeatmapColdColor() {

  }

  @FXML
  private void updateHeatmapHotColor() {

  }

  @FXML
  private void highlightVariable() {

  }

  @FXML
  private void clearHighlightVariable() {

  }

  @FXML
  private void screenshot() {

  }

  @FXML
  private void openScreenShotFolder() {

  }

  @FXML
  private void startOrStopRecording() {

  }

  @FXML
  private void pauseOrContinueRecording() {

  }

  @FXML
  private void pauseOrContinueVisualization() {

  }

  @FXML
  private void relayout() {

  }

  @FXML
  private void seekToUpdate() {

  }

  // METHODS (OTHER)

  public void onClauseUpdate() {

  }

}
