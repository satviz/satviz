package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ConsumerMode;
import edu.kit.satviz.consumer.config.HeatmapColors;
import edu.kit.satviz.consumer.config.WeightFactor;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;

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
  private VBox modeVBox;
  @FXML
  private Button runButton;


  // ATTRIBUTES (OTHER)

  private ConfigController modeConfigController;
  private File satInstanceFile;


  // METHODS (FXML)

  @FXML
  private void initialize() {
    recordingFileLabel.setText(ConsumerConfig.DEFAULT_VIDEO_TEMPLATE_PATH);

    weightFactorChoiceBox.setItems(FXCollections.observableArrayList(WeightFactor.values()));
    weightFactorChoiceBox.setValue(ConsumerConfig.DEFAULT_WEIGHT_FACTOR);

    initializeWindowSizeSpinner();

    coldColorColorPicker.setValue(parseColor(HeatmapColors.DEFAULT_FROM_COLOR));
    hotColorColorPicker.setValue(parseColor(HeatmapColors.DEFAULT_TO_COLOR));

    modeChoiceBox.setItems(FXCollections.observableArrayList(ConsumerMode.values()));
    modeChoiceBox.setValue(ConsumerConfig.DEFAULT_CONSUMER_MODE);
  }

  private void initializeWindowSizeSpinner() {
    SpinnerValueFactory<Integer> windowSizeSpinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
        ConsumerConfig.MIN_WINDOW_SIZE, ConsumerConfig.MAX_WINDOW_SIZE, ConsumerConfig.DEFAULT_WINDOW_SIZE);

    // catch exception when (value is null & (enter/arrow up/arrow down) is pressed)
    windowSizeSpinnerValueFactory.setConverter(new StringConverter<>() {
      @Override
      public String toString(Integer object) {
        return object.toString();
      }

      @Override
      public Integer fromString(String string) {
        try {
          return Integer.parseInt(string);
        } catch (NumberFormatException e) {
          return ConsumerConfig.DEFAULT_WINDOW_SIZE;
        }
      }
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
    if (showLiveVisualizationCheckBox.isSelected()) {
      recordFromStartCheckBox.setDisable(false);
    } else {
      recordFromStartCheckBox.setDisable(true);
      recordFromStartCheckBox.setSelected(true);
    }
  }

  @FXML
  private void selectSatInstanceFile() {
    FileChooser fileChooser = new FileChooser();
    FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Shared Libraries", "*.so");
    fileChooser.getExtensionFilters().add(filter);

    File satInstanceFile = fileChooser.showOpenDialog(null);
    if (satInstanceFile != null) {
      this.satInstanceFile = satInstanceFile;
      satInstanceFileLabel.setText(satInstanceFile.getName());
    }
  }

  @FXML
  private void updateMode() {
    // retrieve name of new fxml-file for mode specific input
    String modeString = modeChoiceBox.getValue().toString().toLowerCase() + "-config.fxml";
    // set vbox content to fxml-file for mode specific input
    FXMLLoader loader = new FXMLLoader(getClass().getResource(modeString));
    modeVBox.getChildren().clear();
    try {
      modeVBox.getChildren().add(loader.load());
    } catch (IOException e) {
      e.printStackTrace();
    }
    // remember controller of current mode
    modeConfigController = loader.getController();
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
