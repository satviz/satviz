package edu.kit.satviz.consumer.gui.visualization;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.processing.Mediator;
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
  private Spinner<Integer> receivedClausesSpinner;
  @FXML
  private Slider receivedClausesSlider;
  @FXML
  private Button relayoutButton;

  // ATTRIBUTES (OTHER)

  private Mediator mediator;
  private ConsumerConfig config;


  // CONSTRUCTORS

  public VisualizationController(Mediator mediator, ConsumerConfig config) {
    this.mediator = mediator;
    this.config = config;
  }

  // METHODS (FXML)

  @FXML
  private void initialize() {

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
