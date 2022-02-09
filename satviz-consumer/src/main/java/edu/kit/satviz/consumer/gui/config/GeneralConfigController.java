package edu.kit.satviz.consumer.gui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ConsumerMode;
import edu.kit.satviz.consumer.config.ConsumerModeConfig;
import edu.kit.satviz.consumer.config.HeatmapColors;
import edu.kit.satviz.consumer.config.WeightFactor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import edu.kit.satviz.consumer.config.json.ModeConfigAdapterFactory;
import edu.kit.satviz.consumer.config.json.PathAdapter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

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
  private VBox modeVbox;
  @FXML
  private Label errorLabel;
  @FXML
  private Button runButton;


  // ATTRIBUTES (OTHER)

  private Gson gson;
  private ModeConfigController modeConfigController;
  private String recordingFile;
  private File satInstanceFile;

  private ConsumerConfig consumerConfig;


  // METHODS (FXML)

  @Override
  protected void initializeComponents() {
    weightFactorChoiceBox.setItems(FXCollections.observableArrayList(WeightFactor.values()));

    initializeIntegerSpinner(windowSizeSpinner,
        ConsumerConfig.MIN_WINDOW_SIZE,
        ConsumerConfig.MAX_WINDOW_SIZE,
        ConsumerConfig.DEFAULT_WINDOW_SIZE);

    modeChoiceBox.setItems(FXCollections.observableArrayList(ConsumerMode.values()));
  }

  @FXML
  private void loadSettings() {
    FileChooser fileChooser = new FileChooser();
    FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JSON Files", "*.json");
    fileChooser.getExtensionFilters().add(filter);

    File file = fileChooser.showOpenDialog(null);
    if (file == null) {
      return;
    }

    ConsumerConfig config = null;
    try {
      config = getGson().fromJson(new FileReader(file), ConsumerConfig.class);
    } catch (FileNotFoundException e) {
      errorLabel.setText("The settings file could not be found.");
      return;
    } catch (JsonSyntaxException | JsonIOException e) {
      errorLabel.setText("The settings file could not be read.");
      return;
    }

    setDefaultValues();

    ConsumerModeConfig modeConfig = config.getModeConfig();
    if (modeConfig != null) {
      modeChoiceBox.setValue(modeConfig.getMode());
      updateMode();
      modeConfigController.loadSettings(modeConfig);
    }

    config.getInstancePath();

    config.isNoGui();

    config.getVideoTemplatePath();

    config.isRecordImmediately();

    config.getWeightFactor();

    config.getWindowSize();

    config.getHeatmapColors();
  }

  @FXML
  private void saveSettings() {

  }

  @FXML
  private void selectRecordingFile() {
    FileChooser fileChooser = new FileChooser();
    FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Video Files", "*.ogv");
    fileChooser.getExtensionFilters().add(filter);

    File file = fileChooser.showSaveDialog(null);
    if (file != null) {
      recordingFile = file.getAbsolutePath();
      recordingFileLabel.setText(file.getName());
    }
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
    FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("SAT Instances", "*.cnf");
    fileChooser.getExtensionFilters().add(filter);

    File file = fileChooser.showOpenDialog(null);
    if (file != null) {
      satInstanceFile = file;
      satInstanceFileLabel.setText(file.getName());
    }
  }

  @FXML
  private void updateMode() {
    // retrieve name of new fxml-file for mode specific input
    String modeString = modeChoiceBox.getValue().toString().toLowerCase() + "-config.fxml";
    // set vbox content to fxml-file for mode specific input
    FXMLLoader loader = new FXMLLoader(getClass().getResource(modeString));
    modeVbox.getChildren().clear();
    try {
      modeVbox.getChildren().add(loader.load());
    } catch (IOException e) {
      e.printStackTrace();
    }
    // remember controller of current mode
    modeConfigController = loader.getController();
  }

  @FXML
  private void run() {
    try {
      consumerConfig = createConsumerConfig();
      Platform.exit();
    } catch (ConfigArgumentException e) {
      errorLabel.setText(e.getMessage());
    }
  }

  // METHODS (OTHER)

  @Override
  protected void setDefaultValues() {
    recordingFile = ConsumerConfig.DEFAULT_VIDEO_TEMPLATE_PATH;
    recordingFileLabel.setText(recordingFile.substring(recordingFile.lastIndexOf("/") + 1));

    showLiveVisualizationCheckBox.setSelected(!ConsumerConfig.DEFAULT_NO_GUI);

    recordFromStartCheckBox.setSelected(ConsumerConfig.DEFAULT_RECORD_IMMEDIATELY);

    weightFactorChoiceBox.setValue(ConsumerConfig.DEFAULT_WEIGHT_FACTOR);

    windowSizeSpinner.getValueFactory().setValue(ConsumerConfig.DEFAULT_WINDOW_SIZE);

    coldColorColorPicker.setValue(intToColor(HeatmapColors.DEFAULT_FROM_COLOR));

    hotColorColorPicker.setValue(intToColor(HeatmapColors.DEFAULT_TO_COLOR));

    satInstanceFile = null;
    satInstanceFileLabel.setText("");

    modeChoiceBox.setValue(ConsumerConfig.DEFAULT_CONSUMER_MODE);
    updateMode();
  }

  @Override
  protected ConsumerConfig createConsumerConfig() throws ConfigArgumentException {
    // creates config where ConsumerModeConfig has already been set
    ConsumerConfig config = modeConfigController.createConsumerConfig();

    if (satInstanceFile == null) {
      throw new ConfigArgumentException("Please select a SAT instance file.");
    }
    config.setInstancePath(satInstanceFile.toPath());

    config.setNoGui(!showLiveVisualizationCheckBox.isSelected());

    config.setVideoTemplatePath(recordingFile);

    config.setRecordImmediately(recordFromStartCheckBox.isSelected());

    config.setWeightFactor(weightFactorChoiceBox.getValue());

    config.setWindowSize(windowSizeSpinner.getValue());

    HeatmapColors colors = new HeatmapColors();
    colors.setFromColor(colorToInt(coldColorColorPicker.getValue()));
    colors.setToColor(colorToInt(hotColorColorPicker.getValue()));
    config.setHeatmapColors(colors);

    return config;
  }

  public ConsumerConfig getConsumerConfig() {
    return consumerConfig;
  }

  private Gson getGson() {
    if (gson == null) {
      gson = new GsonBuilder()
          .setPrettyPrinting()
          .registerTypeAdapterFactory(new ModeConfigAdapterFactory())
          .registerTypeHierarchyAdapter(Path.class, new PathAdapter())
          .create();
    }
    return gson;
  }

  private Color intToColor(int color) {
    int red = (color >>> 16) & 0xFF;
    int green = (color >>> 8) & 0xFF;
    int blue = color & 0xFF;
    return new Color(red / 255.0, green / 255.0, blue / 255.0, 1.0);
  }

  private int colorToInt(Color color) {
    int red = (int) Math.round(color.getRed() * 255);
    int green = (int) Math.round(color.getGreen() * 255);
    int blue = (int) Math.round(color.getBlue() * 255);
    return (red << 16) | (green << 8) | blue;
  }

}
