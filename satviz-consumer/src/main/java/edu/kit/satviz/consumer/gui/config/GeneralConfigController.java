package edu.kit.satviz.consumer.gui.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ConsumerMode;
import edu.kit.satviz.consumer.config.ConsumerModeConfig;
import edu.kit.satviz.consumer.config.HeatmapColors;
import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.gui.GuiUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import edu.kit.satviz.consumer.processing.Heatmap;
import edu.kit.satviz.consumer.processing.HeatmapImplementation;
import edu.kit.satviz.consumer.processing.VariableInteractionGraph;
import edu.kit.satviz.consumer.processing.VariableInteractionGraphImplementation;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controls the general configuration window.
 */
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
  private Spinner<Integer> bufferSizeSpinner;
  @FXML
  private ChoiceBox<HeatmapImplementation> heatmapImplementationChoiceBox;
  @FXML
  private Spinner<Integer> windowSizeSpinner;
  @FXML
  private ColorPicker coldColorColorPicker;
  @FXML
  private ColorPicker hotColorColorPicker;
  @FXML
  private ChoiceBox<VariableInteractionGraphImplementation> vigImplementationChoiceBox;
  @FXML
  private Spinner<Integer> contractionIterationsSpinner;
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

  private ObjectMapper mapper;
  private String recordingFile;
  private File satInstanceFile;
  private ConfigController modeConfigController;

  private ConsumerConfig currentConfig = new ConsumerConfig();
  private ConsumerConfig runConfig = null;
  private boolean run = false;


  // METHODS (FXML)

  @FXML
  private void loadSettings() {
    FileChooser fileChooser = new FileChooser();
    var filter = new FileChooser.ExtensionFilter("JSON Files", "*.json");
    fileChooser.getExtensionFilters().addAll(filter, GuiUtils.ALL_FILES);
    File file = fileChooser.showOpenDialog(null);
    if (file == null) {
      return;
    }

    ConsumerConfig config = null;
    try {
      config = getMapper().readValue(file, ConsumerConfig.class);
    } catch (IOException e) {
      errorLabel.setText("The settings file could not be read.");
      return;
    }

    setDefaultValues();

    // override all settings that are defined in the JSON file
    loadConsumerConfig(config);
  }

  @FXML
  private void saveSettings() {
    FileChooser fileChooser = new FileChooser();
    var filter = new FileChooser.ExtensionFilter("JSON Files", "*.json");
    fileChooser.getExtensionFilters().addAll(filter, GuiUtils.ALL_FILES);

    File file = fileChooser.showSaveDialog(null);
    if (file == null) {
      return;
    }

    setConsumerConfigValues(currentConfig);

    try {
      getMapper().writeValue(file, currentConfig);
    } catch (IOException e) {
      errorLabel.setText("The settings file could not be saved.");
    }
  }

  @FXML
  private void selectRecordingFile() {
    FileChooser fileChooser = new FileChooser();
    var filter = new FileChooser.ExtensionFilter("OGV Files", "*.ogv");
    fileChooser.getExtensionFilters().addAll(filter, GuiUtils.ALL_FILES);

    File file = fileChooser.showSaveDialog(null);
    if (file != null) {
      setRecordingFile(file);
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
    var filter = new FileChooser.ExtensionFilter(
        "SAT Instances", "*.cnf", "*.cnf.xz");
    fileChooser.getExtensionFilters().addAll(filter, GuiUtils.ALL_FILES);

    File file = fileChooser.showOpenDialog(null);
    if (file != null) {
      setSatInstanceFile(file);
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
    setConsumerConfigValues(currentConfig);
    try {
      validateConsumerConfig(currentConfig);
      runConfig = currentConfig;
      ((Stage) runButton.getScene().getWindow()).close(); //runButton is just an arbitrary component
      synchronized (GuiUtils.CONFIG_MONITOR) {
        run = true;
        GuiUtils.CONFIG_MONITOR.notifyAll();
      }
    } catch (ConfigArgumentException e) {
      errorLabel.setText(e.getMessage());
    }
  }

  // METHODS (OTHER)

  @Override
  protected void initializeComponents() {
    weightFactorChoiceBox.setItems(FXCollections.observableArrayList(WeightFactor.values()));

    GuiUtils.initializeIntegerSpinner(bufferSizeSpinner,
        ConsumerConfig.MIN_BUFFER_SIZE,
        ConsumerConfig.MAX_BUFFER_SIZE,
        ConsumerConfig.DEFAULT_BUFFER_SIZE,
        ConsumerConfig.STEP_AMOUNT_BUFFER_SIZE);

    heatmapImplementationChoiceBox.setItems(
        FXCollections.observableArrayList(HeatmapImplementation.values()));

    GuiUtils.initializeIntegerSpinner(windowSizeSpinner,
        ConsumerConfig.MIN_WINDOW_SIZE,
        ConsumerConfig.MAX_WINDOW_SIZE,
        ConsumerConfig.DEFAULT_WINDOW_SIZE,
        ConsumerConfig.STEP_AMOUNT_WINDOW_SIZE);

    vigImplementationChoiceBox.setItems(
        FXCollections.observableArrayList(VariableInteractionGraphImplementation.values()));

    GuiUtils.initializeIntegerSpinner(contractionIterationsSpinner,
        ConsumerConfig.MIN_CONTRACTION_ITERATIONS,
        ConsumerConfig.MAX_CONTRACTION_ITERATIONS,
        ConsumerConfig.DEFAULT_CONTRACTION_ITERATIONS,
        ConsumerConfig.STEP_AMOUNT_CONTRACTION_ITERATIONS);

    modeChoiceBox.setItems(FXCollections.observableArrayList(ConsumerMode.values()));
  }

  @Override
  protected void setDefaultValues() {
    setRecordingFile(ConsumerConfig.DEFAULT_VIDEO_TEMPLATE_PATH);

    showLiveVisualizationCheckBox.setSelected(!ConsumerConfig.DEFAULT_NO_GUI);

    recordFromStartCheckBox.setSelected(ConsumerConfig.DEFAULT_RECORD_IMMEDIATELY);

    weightFactorChoiceBox.setValue(ConsumerConfig.DEFAULT_WEIGHT_FACTOR);

    bufferSizeSpinner.getValueFactory().setValue(ConsumerConfig.DEFAULT_BUFFER_SIZE);

    heatmapImplementationChoiceBox.setValue(Heatmap.DEFAULT_IMPLEMENTATION);

    windowSizeSpinner.getValueFactory().setValue(ConsumerConfig.DEFAULT_WINDOW_SIZE);

    coldColorColorPicker.setValue(GuiUtils.intToColor(HeatmapColors.DEFAULT_FROM_COLOR));

    hotColorColorPicker.setValue(GuiUtils.intToColor(HeatmapColors.DEFAULT_TO_COLOR));

    vigImplementationChoiceBox.setValue(VariableInteractionGraph.DEFAULT_IMPLEMENTATION);

    contractionIterationsSpinner.getValueFactory().setValue(
        ConsumerConfig.DEFAULT_CONTRACTION_ITERATIONS);

    satInstanceFile = null;
    satInstanceFileLabel.setText("");

    setConsumerMode(ConsumerConfig.DEFAULT_CONSUMER_MODE);
  }

  @Override
  protected void loadConsumerConfig(ConsumerConfig config) {
    if (config == null) {
      return;
    }

    // assume the given config contains all relevant values -> no validation
    currentConfig = config;

    ConsumerModeConfig modeConfig = config.getModeConfig();
    if (modeConfig != null && modeConfig.getMode() != null) {
      setConsumerMode(modeConfig.getMode());
      modeConfigController.loadConsumerConfig(config);
    }

    Path instancePath = config.getInstancePath();
    if (instancePath != null) {
      setSatInstanceFile(instancePath.toFile());
    }

    showLiveVisualizationCheckBox.setSelected(!config.isNoGui());

    String videoTemplatePath = config.getVideoTemplatePath();
    if (videoTemplatePath != null) {
      setRecordingFile(videoTemplatePath);
    }

    recordFromStartCheckBox.setSelected(config.isRecordImmediately());

    WeightFactor weightFactor = config.getWeightFactor();
    if (weightFactor != null) {
      weightFactorChoiceBox.setValue(weightFactor);
    }

    bufferSizeSpinner.getValueFactory().setValue(config.getBufferSize());

    heatmapImplementationChoiceBox.setValue(config.getHeatmapImplementation());

    windowSizeSpinner.getValueFactory().setValue(config.getWindowSize());

    HeatmapColors colors = config.getHeatmapColors();
    if (colors != null) {
      coldColorColorPicker.setValue(GuiUtils.intToColor(colors.getFromColor()));
      hotColorColorPicker.setValue(GuiUtils.intToColor(colors.getToColor()));
    }

    vigImplementationChoiceBox.setValue(config.getVigImplementation());

    contractionIterationsSpinner.getValueFactory().setValue(config.getContractionIterations());
  }

  @Override
  protected void setConsumerConfigValues(ConsumerConfig config) {
    // set ConsumerModeConfig
    modeConfigController.setConsumerConfigValues(config);

    if (satInstanceFile != null) {
      config.setInstancePath(satInstanceFile.toPath());
    }

    config.setNoGui(!showLiveVisualizationCheckBox.isSelected());

    config.setVideoTemplatePath(recordingFile);

    config.setRecordImmediately(recordFromStartCheckBox.isSelected());

    config.setWeightFactor(weightFactorChoiceBox.getValue());

    config.setBufferSize(bufferSizeSpinner.getValue());

    config.setHeatmapImplementation(heatmapImplementationChoiceBox.getValue());

    config.setWindowSize(windowSizeSpinner.getValue());

    HeatmapColors colors = new HeatmapColors();
    colors.setFromColor(GuiUtils.colorToInt(coldColorColorPicker.getValue()));
    colors.setToColor(GuiUtils.colorToInt(hotColorColorPicker.getValue()));
    config.setHeatmapColors(colors);

    config.setVigImplementation(vigImplementationChoiceBox.getValue());

    config.setContractionIterations(contractionIterationsSpinner.getValue());
  }

  @Override
  protected void validateConsumerConfig(ConsumerConfig config) throws ConfigArgumentException {
    if (config.getInstancePath() == null) {
      throw new ConfigArgumentException("Please select a SAT instance file.");
    }
    modeConfigController.validateConsumerConfig(config);
  }

  private void setRecordingFile(String path) {
    recordingFile = path;
    recordingFileLabel.setText(new File(path).getName());
  }

  private void setRecordingFile(File file) {
    recordingFile = file.getAbsolutePath();
    recordingFileLabel.setText(file.getName());
  }

  private void setSatInstanceFile(File file) {
    satInstanceFile = file;
    satInstanceFileLabel.setText(file.getName());
  }

  private void setConsumerMode(ConsumerMode mode) {
    modeChoiceBox.setValue(mode);
    updateMode();
  }

  private ObjectMapper getMapper() {
    // only create mapper once
    if (mapper == null) {
      mapper = new ObjectMapper();
      mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
      SimpleModule m = new SimpleModule("PathToString");
      m.addSerializer(Path.class, new ToStringSerializer());
      mapper.registerModule(m);
    }
    return mapper;
  }

  /**
   * Returns whether the window which this class controls
   * has been closed by clicking the run button.
   *
   * @return Whether the window has been closed by clicking the run button.
   */
  public boolean hasRun() {
    return run;
  }

  /**
   * Retrieves the configuration set by the user.
   *
   * @return The {@link ConsumerConfig} object which contains the configuration parameters set by
   *         the user.
   */
  public ConsumerConfig getConsumerConfig() {
    return runConfig;
  }

}
