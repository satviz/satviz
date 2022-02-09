package edu.kit.satviz.consumer.gui.visualization;

import edu.kit.satviz.consumer.config.WeightFactor;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;

public class VisualizationController {

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

  // TODO: add constructor with Mediator parameter

  public void onClauseUpdate() {

  }

}
