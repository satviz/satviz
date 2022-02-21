package edu.kit.satviz.consumer.gui.visualization;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Starts the control window for the live visualization.
 * Requires the {@link VisualizationController} of the window to be set before opening the window.
 */
public class VisualizationStarter extends Application {

  private static VisualizationController visualizationController;

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader fxmlLoader = new FXMLLoader(
        VisualizationStarter.class.getResource("visualization.fxml"));
    fxmlLoader.setController(visualizationController);

    Scene scene = new Scene(fxmlLoader.load());
    primaryStage.setTitle("Visualization");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.show();
  }

  /**
   * Sets the {@code controller} of the visualization window.
   * This method must be called before the visualization window is opened.
   *
   * @param controller The {@code controller} which controls the visualization window.
   */
  public static void setVisualizationController(VisualizationController controller) {
    visualizationController = controller;
  }

}
