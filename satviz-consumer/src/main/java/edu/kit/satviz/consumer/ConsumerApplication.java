package edu.kit.satviz.consumer;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.gui.config.ConfigStarter;
import edu.kit.satviz.consumer.gui.visualization.VisualizationController;
import edu.kit.satviz.consumer.gui.visualization.VisualizationStarter;
import edu.kit.satviz.consumer.processing.ClauseCoordinator;
import edu.kit.satviz.consumer.processing.Heatmap;
import edu.kit.satviz.consumer.processing.Mediator;
import edu.kit.satviz.consumer.processing.VariableInteractionGraph;
import edu.kit.satviz.network.ConsumerConnectionListener;
import edu.kit.satviz.network.ProducerId;
import edu.kit.satviz.parsers.DimacsFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ConsumerApplication {

  public static void main(String[] args) throws FileNotFoundException {
    ConsumerConfig config = getStartingConfig(args);
    int variableAmount;
    try (DimacsFile dimacsFile = new DimacsFile(new FileInputStream(config.getInstancePath().toString()))) {
      variableAmount = dimacsFile.getVariableAmount();
    }
    int port = 0; // TODO: Either EMBEDDED port or in ConsumerConfig
    Graph graph = Graph.create(variableAmount);
    ClauseCoordinator coordinator = new ClauseCoordinator(graph, null); // TODO: get tempDir!
    Heatmap heatmap = new Heatmap();
    VariableInteractionGraph vig = new VariableInteractionGraph();

    Mediator mediator = null; // TODO: Get Mediator!
    ConsumerConnectionListener connectionListener = setupNetworkConnection(port);
    ProducerId producerId = waitForFirstProducer(connectionListener);

    if (!config.isNoGui()) {
      VisualizationController vController = new VisualizationController(mediator, config);
      coordinator.registerChangeListener(vController::onClauseUpdate);

      VisualizationStarter.setVisualizationController(vController);
      VisualizationStarter.launch();
    }

    // TODO: Connect to Producer!
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

  private static ConsumerConnectionListener setupNetworkConnection(int port) {
    // TODO: 16.02.2022
    return null;
  }

  private static ProducerId waitForFirstProducer(ConsumerConnectionListener connectionListener) {
    // TODO: 16.02.2022
    return null;
  }

}
