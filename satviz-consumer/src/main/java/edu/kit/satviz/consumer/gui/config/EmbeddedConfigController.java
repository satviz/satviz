package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.EmbeddedModeConfig;
import edu.kit.satviz.consumer.config.EmbeddedModeSource;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

public class EmbeddedConfigController extends ConfigController {

  @FXML
  private ChoiceBox<EmbeddedModeSource> producerModeChoiceBox;
  @FXML
  private Button producerModeFileButton;
  @FXML
  private Label producerModeFileLabel;


  @FXML
  private void initialize() {
    producerModeChoiceBox.setItems(FXCollections.observableArrayList(EmbeddedModeSource.values()));
    producerModeChoiceBox.setValue(EmbeddedModeConfig.DEFAULT_EMBEDDED_MODE_SOURCE);
  }

  @FXML
  private void updateProducerMode() {

  }

  @FXML
  private void selectProducerModeFile() {

  }

  @Override
  protected void run() {

  }

}
