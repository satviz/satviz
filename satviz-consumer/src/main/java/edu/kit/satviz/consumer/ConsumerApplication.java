package edu.kit.satviz.consumer;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ConsumerMode;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import edu.kit.satviz.consumer.display.DisplayType;
import edu.kit.satviz.consumer.display.VideoController;
import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.gui.config.ConfigStarter;
import edu.kit.satviz.consumer.gui.visualization.VisualizationController;
import edu.kit.satviz.consumer.gui.visualization.VisualizationStarter;
import edu.kit.satviz.consumer.processing.ClauseCoordinator;
import edu.kit.satviz.consumer.processing.Heatmap;
import edu.kit.satviz.consumer.processing.Mediator;
import edu.kit.satviz.consumer.processing.VariableInteractionGraph;
import edu.kit.satviz.network.ConsumerConnection;
import edu.kit.satviz.network.ProducerId;
import edu.kit.satviz.parsers.DimacsFile;
import edu.kit.satviz.parsers.ParsingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import javafx.application.Application;

public final class ConsumerApplication {

  private static ProducerId pid = null;
  private static final Object SYNC_OBJECT = new Object();

  public static void main(String[] args) throws IOException, InterruptedException {
    ConsumerConfig config = getStartingConfig(args);
    int variableAmount;
    try {
      try (DimacsFile dimacsFile = new DimacsFile(
          new FileInputStream(config.getInstancePath().toString()))) {
        variableAmount = dimacsFile.getVariableAmount();
      }
    } catch (ParsingException e) {
      if (!config.isNoGui()) {
        // Error window.
      }
      throw e;
    }

    Mediator.MediatorBuilder mediatorBuilder = new Mediator.MediatorBuilder();
    mediatorBuilder.setConfig(config);

    Graph graph = Graph.create(variableAmount);
    mediatorBuilder.setGraph(graph);
    VideoController videoController = VideoController.create(
        graph,
        (config.isNoGui()) ? DisplayType.OFFSCREEN : DisplayType.ONSCREEN,
        1000,
        700
    );
    mediatorBuilder.setController(videoController);
    ClauseCoordinator coordinator = new ClauseCoordinator(
        graph,
        Files.createTempDirectory("satviz"),
        variableAmount
    );
    mediatorBuilder.setCoordinator(coordinator);
    Heatmap heatmap = new Heatmap(variableAmount);
    mediatorBuilder.setHeatmap(heatmap);
    VariableInteractionGraph vig = new VariableInteractionGraph(config.getWeightFactor());
    mediatorBuilder.setVig(vig);

    ConsumerConnection connection = setupNetworkConnection(config);

    Mediator mediator = mediatorBuilder.createMediator();

    if (!config.isNoGui()) {
      VisualizationController visController = new VisualizationController(mediator, config);
      coordinator.registerChangeListener(visController::onClauseUpdate);

      VisualizationStarter.setVisualizationController(visController);
      Application.launch(VisualizationStarter.class);
    }

    connection.connect(ConsumerApplication.pid, mediator);
    mediator.startOrStopRecording();
    mediator.pauseOrContinueVisualization();



    //coordinator.close();
    //videoController.close();
  }

  private static ConsumerConfig getStartingConfig(String[] args) {
    if (args.length == 0) {
      Application.launch(ConfigStarter.class);
      return ConfigStarter.getConsumerConfig();
    } else {
      // TODO: parse Arguments from CLI
      return null;
    }
  }

  private static ConsumerConnection setupNetworkConnection(ConsumerConfig config)
      throws InterruptedException {
    int consumerPort;
    boolean isEmbedded = config.getModeConfig().getMode() == ConsumerMode.EMBEDDED;
    if (isEmbedded) {
      consumerPort = 0;
    } else {
      consumerPort = ((ExternalModeConfig) config.getModeConfig()).getPort();
    }
    ConsumerConnection connection = new ConsumerConnection(consumerPort);
    connection.registerConnect(ConsumerApplication::newConnectionAvailable);
    if (isEmbedded) {
      // TODO: 19.02.2022 START PRODUCER!
      connection.getPort();
    }
    synchronized (SYNC_OBJECT) {
      while (ConsumerApplication.pid == null) {
        SYNC_OBJECT.wait();
      }
    }
    return connection;
  }

  private static void newConnectionAvailable(ProducerId pid) {
    synchronized (SYNC_OBJECT) {
      if (ConsumerApplication.pid == null) {
        ConsumerApplication.pid = pid;
        SYNC_OBJECT.notifyAll();
      }
    }
  }

}
