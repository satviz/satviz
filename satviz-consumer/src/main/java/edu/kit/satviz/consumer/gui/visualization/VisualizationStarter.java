package edu.kit.satviz.consumer.gui.visualization;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

  public static void setVisualizationController(VisualizationController controller) {
    visualizationController = controller;
  }

}
