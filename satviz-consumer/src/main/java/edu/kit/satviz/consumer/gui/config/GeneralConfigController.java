package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ConsumerMode;
import edu.kit.satviz.consumer.config.HeatmapColors;
import edu.kit.satviz.consumer.config.WeightFactor;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;

public class GeneralConfigController extends ConfigController {

  // ATTRIBUTES (FXML)

  @FXML
  private Button loadSettingsButton;
  @FXML
  private Button saveSettingsButton;
  @FXML
  private Button recordingFileButton;
  @FXML
  private Label recordingFileLabel;
  @FXML
  private CheckBox showLiveVisualizationCheckBox;
  @FXML
  private CheckBox recordFromStartCheckBox;
  @FXML
  private ChoiceBox<WeightFactor> weightFactorChoiceBox;
  @FXML
  private Spinner<Integer> windowSizeSpinner;
  @FXML
  private ColorPicker coldColorColorPicker;
  @FXML
  private ColorPicker hotColorColorPicker;
  @FXML
  private Button satInstanceFileButton;
  @FXML
  private Label satInstanceFileLabel;
  @FXML
  private ChoiceBox<ConsumerMode> modeChoiceBox;
  @FXML
  private Button runButton;


  // METHODS (FXML)

  @FXML
  private void initialize() {
    recordingFileLabel.setText(ConsumerConfig.DEFAULT_VIDEO_TEMPLATE_PATH);

    weightFactorChoiceBox.setItems(FXCollections.observableArrayList(WeightFactor.values()));
    weightFactorChoiceBox.setValue(ConsumerConfig.DEFAULT_WEIGHT_FACTOR);

    // TODO: add constants in ConsumerConfig
    SpinnerValueFactory<Integer> windowSizeSpinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
        ConsumerConfig.MIN_WINDOW_SIZE, ConsumerConfig.MAX_WINDOW_SIZE, ConsumerConfig.DEFAULT_WINDOW_SIZE);
    windowSizeSpinnerValueFactory.valueProperty().addListener((observableValue, oldValue, newValue) -> {
      // prevent exception for value being null (allow spinner to be empty)
      if (newValue == null) {
        // do nothing
      }
      // TODO: catch exception when (value is null & enter is pressed) -> display error message
    });

    windowSizeSpinner.setValueFactory(windowSizeSpinnerValueFactory);

    windowSizeSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.equals("")) {
        try {
          Integer.parseInt(newValue);
        } catch (NumberFormatException e) {
          windowSizeSpinner.getEditor().setText(oldValue);
        }
      }
    });

    coldColorColorPicker.setValue(parseColor(HeatmapColors.DEFAULT_FROM_COLOR));
    hotColorColorPicker.setValue(parseColor(HeatmapColors.DEFAULT_TO_COLOR));

    modeChoiceBox.setItems(FXCollections.observableArrayList(ConsumerMode.values()));
    // TODO: add constant in ConsumerConfig
    modeChoiceBox.setValue(ConsumerConfig.DEFAULT_CONSUMER_MODE);
  }

  @FXML
  private void loadSettings() {

  }

  @FXML
  private void saveSettings() {

  }

  @FXML
  private void selectRecordingFile() {

  }

  @FXML
  private void setLiveVisualization() {

  }

  @FXML
  private void selectSatInstanceFile() {

  }

  @FXML
  private void updateMode() {

  }

  @Override
  @FXML
  protected void run() {

  }

  // METHODS (OTHER)

  private Color parseColor(int color) {
    int red = color >>> (4 * 4);
    int green = (color >>> (2 * 4)) % (int) Math.pow(2.0, 2.0 * 4.0);
    int blue = color % (int) Math.pow(2.0, 2.0 * 4.0);
    return new Color(red / 255.0, green / 255.0, blue / 255.0, 1.0);
  }

}