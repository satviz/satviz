package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.gui.GuiUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Starts the configuration window of the application.
 * Allows to retrieve the resulting {@link ConsumerConfig} object after the configuration has been
 * finished.
 */
public class ConfigStarter extends Application {

  private static GeneralConfigController configController = null;
  private static boolean closed = false;

  @Override
  public void start(Stage primaryStage) throws Exception {
    configController = new GeneralConfigController();

    FXMLLoader fxmlLoader = new FXMLLoader(ConfigStarter.class.getResource("general-config.fxml"));
    fxmlLoader.setController(configController);

    Scene scene = new Scene(fxmlLoader.load());
    primaryStage.setTitle("Configuration");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.getScene().getWindow().setOnCloseRequest(event -> {
      synchronized (GuiUtils.CONFIG_MONITOR) {
        closed = true;
        GuiUtils.CONFIG_MONITOR.notifyAll();
      }
    });
    primaryStage.show();
  }

  /**
   * Returns whether the configuration window has been closed.
   *
   * @return Whether the configuration window has been closed.
   */
  public static boolean isDone() {
    return (configController != null && configController.hasRun()) || closed;
  }

  /**
   * Retrieve the configuration set by the user.
   *
   * @return The {@link ConsumerConfig} object which contains the configuration parameters set by
   *         the user.
   */
  public static ConsumerConfig getConsumerConfig() {
    return configController.getConsumerConfig();
  }

}
