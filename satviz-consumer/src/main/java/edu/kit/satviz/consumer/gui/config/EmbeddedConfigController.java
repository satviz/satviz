package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.EmbeddedModeConfig;
import edu.kit.satviz.consumer.config.EmbeddedModeSource;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

/**
 * Controls the window for the embedded mode configuration.
 */
public class EmbeddedConfigController extends ConfigController {

  // ATTRIBUTES (FXML)

  @FXML
  private ChoiceBox<EmbeddedModeSource> producerModeChoiceBox;
  @FXML
  private Label producerModeFileLabel;

  // ATTRIBUTES (OTHER)

  private File producerModeFile;


  // METHODS (FXML)

  @FXML
  private void selectProducerModeFile() {
    FileChooser fileChooser = new FileChooser();

    List<String> fileExtensions = producerModeChoiceBox.getValue().getFileExtensions()
        .stream()
        .map(s -> "*" + s)
        .toList();
    FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
        producerModeChoiceBox.getValue().name(), fileExtensions);

    fileChooser.getExtensionFilters().add(filter);

    File file = fileChooser.showOpenDialog(null);
    if (file != null) {
      setProducerModeFile(file);
    }
  }

  // METHODS (OTHER)

  @Override
  protected void initializeComponents() {
    producerModeChoiceBox.setItems(FXCollections.observableArrayList(EmbeddedModeSource.values()));
  }

  @Override
  protected void setDefaultValues() {
    producerModeChoiceBox.setValue(EmbeddedModeConfig.DEFAULT_EMBEDDED_MODE_SOURCE);

    producerModeFile = null;
    producerModeFileLabel.setText("");
  }

  @Override
  protected void loadConsumerConfig(ConsumerConfig config) {
    setDefaultValues();

    // config & config.getModeConfig() have already been checked for null by GeneralConfigController
    EmbeddedModeConfig embeddedModeConfig = (EmbeddedModeConfig) config.getModeConfig();

    EmbeddedModeSource source = embeddedModeConfig.getSource();
    if (source != null) {
      producerModeChoiceBox.setValue(source);
    }

    Path sourcePath = embeddedModeConfig.getSourcePath();
    if (sourcePath != null) {
      setProducerModeFile(sourcePath.toFile());
    }
  }

  @Override
  protected void setConsumerConfigValues(ConsumerConfig config) {
    if (config == null) {
      config = new ConsumerConfig();
    }

    EmbeddedModeConfig modeConfig = new EmbeddedModeConfig();
    modeConfig.setSource(producerModeChoiceBox.getValue());
    if (producerModeFile != null) {
      modeConfig.setSourcePath(producerModeFile.toPath());
    }

    config.setModeConfig(modeConfig);
  }

  @Override
  protected void validateConsumerConfig(ConsumerConfig config) throws ConfigArgumentException {
    EmbeddedModeConfig embeddedModeConfig = (EmbeddedModeConfig) config.getModeConfig();

    if (embeddedModeConfig.getSourcePath() == null) {
      throw new ConfigArgumentException("Please select a "
          + producerModeChoiceBox.getValue().name() + " file.");
    }
  }

  private void setProducerModeFile(File file) {
    producerModeFile = file;
    producerModeFileLabel.setText(file.getName());
  }
}
