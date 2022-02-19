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
import edu.kit.satviz.network.ConsumerConnectionListener;
import edu.kit.satviz.network.ProducerId;
import edu.kit.satviz.parsers.DimacsFile;
import edu.kit.satviz.parsers.ParsingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public final class ConsumerApplication {

  private static ProducerId pid = null;
  private static final Object SYNC_OBJECT = new Object();

  public static void main(String[] args) throws FileNotFoundException, InterruptedException {
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

    Graph graph = Graph.create(variableAmount);
    VideoController videoController = VideoController.create(
        graph,
        (config.isNoGui()) ? DisplayType.OFFSCREEN : DisplayType.ONSCREEN
    );
    ClauseCoordinator coordinator = new ClauseCoordinator(graph, createTempDir());
    Heatmap heatmap = new Heatmap(); //
    VariableInteractionGraph vig = new VariableInteractionGraph();

    ConsumerConnection connection = setupNetworkConnection(config);

    Mediator mediator = null; // MEDIATOR

    if (!config.isNoGui()) {
      VisualizationController vController = new VisualizationController(mediator, config);
      coordinator.registerChangeListener(vController::onClauseUpdate);

      VisualizationStarter.setVisualizationController(vController);
      VisualizationStarter.launch();
    }

    connection.connect(ConsumerApplication.pid, null); // MEDIATOR

    // TODO: Start main-loop
    // TODO: End
    // TODO: Exceptions!
  }

  private static ConsumerConfig getStartingConfig(String[] args) {
    if (args.length == 0) {
      ConfigStarter.launch();
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

  private static ProducerId waitForFirstProducer(ConsumerConnectionListener connectionListener) {
    // TODO: 16.02.2022
    return null;
  }

  private static Path createTempDir() {
    // TODO: 19.02.2022
    return null;
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
