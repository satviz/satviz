package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ConfigStarter extends Application {

  private static GeneralConfigController configController;

  @Override
  public void start(Stage primaryStage) throws Exception {
    configController = new GeneralConfigController();

    FXMLLoader fxmlLoader = new FXMLLoader(ConfigStarter.class.getResource("general-config.fxml"));
    fxmlLoader.setController(configController);

    Scene scene = new Scene(fxmlLoader.load());
    primaryStage.setTitle("Configuration");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.show();
  }

  public static ConsumerConfig getConsumerConfig() {
    return configController.getConsumerConfig();
  }

}
