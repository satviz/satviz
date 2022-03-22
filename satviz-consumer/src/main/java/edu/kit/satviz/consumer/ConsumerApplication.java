package edu.kit.satviz.consumer;

import edu.kit.satviz.common.Compression;
import edu.kit.satviz.common.ConstraintValidationException;
import edu.kit.satviz.common.Hashing;
import edu.kit.satviz.consumer.cli.ConsumerCli;
import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.ConsumerConstraint;
import edu.kit.satviz.consumer.config.ConsumerMode;
import edu.kit.satviz.consumer.config.ConsumerModeConfig;
import edu.kit.satviz.consumer.config.EmbeddedModeConfig;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import edu.kit.satviz.consumer.display.DisplayType;
import edu.kit.satviz.consumer.display.VideoController;
import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.consumer.gui.GuiUtils;
import edu.kit.satviz.consumer.gui.config.ConfigStarter;
import edu.kit.satviz.consumer.gui.visualization.VisualizationController;
import edu.kit.satviz.consumer.gui.visualization.VisualizationStarter;
import edu.kit.satviz.consumer.processing.ArrayNodeMapping;
import edu.kit.satviz.consumer.processing.ClauseCoordinator;
import edu.kit.satviz.consumer.processing.CliqueInteractionGraph;
import edu.kit.satviz.consumer.processing.FrequencyHeatmap;
import edu.kit.satviz.consumer.processing.Heatmap;
import edu.kit.satviz.consumer.processing.IdentityMapping;
import edu.kit.satviz.consumer.processing.Mediator;
import edu.kit.satviz.consumer.processing.RecencyHeatmap;
import edu.kit.satviz.consumer.processing.RingInteractionGraph;
import edu.kit.satviz.consumer.processing.VariableInteractionGraph;
import edu.kit.satviz.network.pub.ConsumerConnection;
import edu.kit.satviz.network.pub.OfferType;
import edu.kit.satviz.network.pub.ProducerId;
import edu.kit.satviz.network.pub.SolverId;
import edu.kit.satviz.parsers.DimacsFile;
import edu.kit.satviz.parsers.ParsingException;
import edu.kit.satviz.sat.ClauseUpdate;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;
import net.lingala.zip4j.ZipFile;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

public final class ConsumerApplication {

  private static final Logger logger = Logger.getLogger("Consumer");
  private static ProducerId pid = null;
  private static final Object SYNC_OBJECT = new Object();

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    logger.setLevel(Level.FINER);
    logger.log(Level.FINER, "Starting consumer with arguments {0}", args);
    ConsumerConfig config = getStartingConfig(args);
    if (config == null) {
      System.exit(0);
      return;
    }

    try {
      new ConsumerConstraint().validate(config);
    } catch (ConstraintValidationException e) {
      logger.severe(e.getMessage());
      System.exit(1);
    }

    Path tempDir = Files.createTempDirectory("satviz"); // TODO: 05.03.2022 make own temp?
    tempDir.toFile().deleteOnExit();

    logger.finer("Reading SAT instance file");
    InitialGraphInfo initialData = readDimacsFile(config);

    logger.info("Setting up network connection");
    ConsumerConnection connection = setupNetworkConnection(config, tempDir);
    logger.log(Level.INFO, "Producer {0} connected", pid);
    if (!verifyInstanceHash(config.getInstancePath())) {
      connection.disconnect(pid);
      System.exit(1);
      return;
    }
    
    ScheduledExecutorService glScheduler = Executors.newSingleThreadScheduledExecutor();
    Supplier<VariableInteractionGraph> vig = () -> getVigImplementation(config);

    Graph.Contraction contraction = contract(config, vig, initialData);
    logger.log(Level.INFO, "Graph contracted to {0} nodes", contraction.remainingNodes());
    GlComponents components = initializeRendering(config, vig, contraction, initialData, glScheduler);

    ClauseCoordinator coordinator = new ClauseCoordinator(components.graph,
        tempDir, initialData.variables, components.nodeMapping);

    Mediator mediator = new Mediator.MediatorBuilder()
        .setConfig(config)
        .setGlScheduler(glScheduler)
        .setController(components.controller)
        .setGraph(components.graph)
        .setCoordinator(coordinator)
        .setHeatmap(getHeatmapImplementation(config))
        .setVig(vig.get())
        .createMediator();

    mediator.registerCloseAction(() -> {
      logger.info("Closing connection (this may take a few seconds...)");
      connection.stop();
    });

    if (!config.isNoGui()) {
      startVisualisationGui(mediator, config, initialData.variables);
    } else {
      mediator.registerFrameAction(new Runnable() {

        final long period = config.getPeriod();
        final int timeout = config.getVideoTimeout() * 1000;
        long frames = 0;

        @Override
        public void run() {
          if (frames++ * period >= timeout) {
            ForkJoinPool.commonPool().execute(() -> {
              try {
                mediator.close();
              } catch (Exception e) {
                e.printStackTrace();
                System.exit(2);
              }
            });
          }
        }
      });
    }

    connection.connect(ConsumerApplication.pid, mediator);

    if (config.isRecordImmediately() || config.isNoGui()) {
      mediator.startOrStopRecording();
    }
    mediator.startRendering();
  }

  private static Heatmap getHeatmapImplementation(ConsumerConfig config) {
    return switch (config.getHeatmapImplementation()) {
      case RECENCY -> new RecencyHeatmap(config.getWindowSize());
      case FREQUENCY_SIZE -> new FrequencyHeatmap(config.getWindowSize(),
          FrequencyHeatmap.HeatStrategy.SIZE);
      case FREQUENCY_MAX_FREQUENCY -> new FrequencyHeatmap(config.getWindowSize(),
          FrequencyHeatmap.HeatStrategy.MAX_FREQUENCY);
    };
  }

  private static VariableInteractionGraph getVigImplementation(ConsumerConfig config) {
    return switch (config.getVigImplementation()) {
      case RING -> new RingInteractionGraph(config.getWeightFactor());
      case CLIQUE -> new CliqueInteractionGraph(config.getWeightFactor());
    };
  }

  private static Graph.Contraction contract(
      ConsumerConfig config,
      Supplier<? extends VariableInteractionGraph> vig,
      InitialGraphInfo initialData
  ) {
    logger.info("Applying graph contraction");
    try (Graph initialGraph = Graph.create(initialData.variables)) {
      initialGraph.submitUpdate(vig.get()
          .process(initialData.clauses, initialGraph, IdentityMapping.INSTANCE));
      return initialGraph.computeContraction(config.getContractionIterations());
    }
  }

  private static GlComponents initializeRendering(
      ConsumerConfig config, Supplier<? extends VariableInteractionGraph> vig,
      Graph.Contraction contraction, InitialGraphInfo initialData, ExecutorService glScheduler
  ) throws InterruptedException, ExecutionException {
    logger.finer("Initialising OpenGL window");

    GlComponents components = glScheduler.submit(() -> {
      Graph graph = Graph.create(contraction.remainingNodes());
      VideoController videoController = VideoController.create(
          graph,
          (config.isNoGui()) ? DisplayType.OFFSCREEN : DisplayType.ONSCREEN,
          1920,
          1080
      );
      videoController.applyTheme(config.getTheme());
      return new GlComponents(graph, new ArrayNodeMapping(contraction.mapping()), videoController);
    }).get();


    logger.info("Calculating initial layout");
    glScheduler.submit(() -> {
      try {
        HeatUpdate u = new HeatUpdate();
        for (int i = 0; i < contraction.remainingNodes(); i++) {
          u.add(i, 0);
        }
        components.graph.submitUpdate(u);
        components.graph.submitUpdate(vig.get().process(
            initialData.clauses, components.graph, components.nodeMapping));
        components.graph.recalculateLayout();
        components.controller.resetCamera();
        components.controller.nextFrame();
      } catch (Throwable e) {
        e.printStackTrace();
        System.exit(1);
      }
    }).get();
    return components;
  }

  private static InitialGraphInfo readDimacsFile(ConsumerConfig config) throws IOException {
    try (DimacsFile dimacsFile = new DimacsFile(
        Compression.openPossiblyCompressed(config.getInstancePath()))) {
      int variableAmount = dimacsFile.getVariableAmount();
      logger.log(Level.INFO, "Instance contains {0} variables", variableAmount);
      ClauseUpdate[] clauses = StreamSupport.stream(dimacsFile.spliterator(), false)
          .toArray(ClauseUpdate[]::new);
      return new InitialGraphInfo(variableAmount, clauses);
    } catch (ParsingException e) {
      if (!config.isNoGui()) {
        // Error window.
      }
      logger.log(Level.SEVERE, "Could not read DIMACS file", e);
      System.exit(1);
      return null;
    }
  }

  private static ConsumerConfig getStartingConfig(String[] args) throws InterruptedException {
    if (args.length == 0) {
      GuiUtils.launch(ConfigStarter.class);
      synchronized (GuiUtils.CONFIG_MONITOR) {
        while (!ConfigStarter.isDone()) {
          GuiUtils.CONFIG_MONITOR.wait();
        }
      }
      return ConfigStarter.getConsumerConfig();
    } else {
      try {
        return ConsumerCli.parseArgs(args);
      } catch (ArgumentParserException e) {
        ConsumerCli.PARSER.handleError(e);
        System.exit(1);
        return null;
      }
    }
  }

  private static ConsumerConnection setupNetworkConnection(ConsumerConfig config, Path tempDir)
      throws InterruptedException {
    ConsumerModeConfig modeConfig = config.getModeConfig();
    boolean embedded = modeConfig.getMode() == ConsumerMode.EMBEDDED;
    int consumerPort = embedded ? 0 : ((ExternalModeConfig) modeConfig).getPort();
    ConsumerConnection connection = new ConsumerConnection(consumerPort,
        ConsumerApplication::newConnectionAvailable, (s) -> logger.log(Level.SEVERE,
        "network fail: {0}", s));
    try {
      connection.start();
      logger.log(Level.INFO, "Port {0} opened", String.valueOf(connection.getPort()));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error while trying to start embedded producer", e);
      System.exit(1);
      return null;
    }

    if (embedded) {
      logger.info("Starting embedded producer");
      EmbeddedModeConfig embedConfig = (EmbeddedModeConfig) modeConfig;
      try {
        String sourcePath = embedConfig.getSourcePath().toAbsolutePath().toString();
        List<String> baseArgs = List.of("-H", InetAddress.getLocalHost().getHostAddress(),
            "-P", String.valueOf(connection.getPort()));
        List<String> additionalArgs = switch (embedConfig.getSource()) {
          case SOLVER -> List.of("-s", sourcePath, "-i", config.getInstancePath().toAbsolutePath().toString());
          case PROOF -> List.of("-p", sourcePath);
        };
        List<String> allArgs = new ArrayList<>();
        allArgs.add("sh");
        allArgs.add("./sat-prod");
        allArgs.addAll(baseArgs);
        allArgs.addAll(additionalArgs);
        Path bin = extractProducer(tempDir);
        new ProcessBuilder()
            .directory(bin.toFile())
            .command(allArgs)
            .inheritIO()
            .start();
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error while trying to start embedded producer", e);
        System.exit(1);
        return null;
      }
    }
    logger.info("Waiting for producer to connect...");
    synchronized (SYNC_OBJECT) {
      while (ConsumerApplication.pid == null) {
        SYNC_OBJECT.wait();
      }
    }
    return connection;
  }

  private static Path extractProducer(Path tempDir) throws IOException {
    Path producerDir = Files.createTempDirectory(tempDir, "producer");
    var producerStream
        = ConsumerApplication.class.getResourceAsStream("/satviz-producer.zip");
    try (producerStream) {
      Path producerZip = producerDir.resolve("producer.zip");
      Files.copy(producerStream, producerZip);
      try (ZipFile zip = new ZipFile(producerZip.toFile())) {
        zip.extractAll(producerDir.toString());
      }
    }
    return producerDir.resolve("satviz-producer/bin/");
  }

  private static void newConnectionAvailable(ProducerId pid) {
    synchronized (SYNC_OBJECT) {
      if (ConsumerApplication.pid == null) {
        ConsumerApplication.pid = pid;
        SYNC_OBJECT.notifyAll();
      }
    }
  }

  private static boolean verifyInstanceHash(Path instancePath)
      throws IOException {
    if (pid.getType() != OfferType.SOLVER) {
      return true;
    }
    long hash = Hashing.hashContent(Files.newInputStream(instancePath));
    SolverId sid = (SolverId) pid;
    if (hash != sid.getInstanceHash()) {
      logger.log(Level.SEVERE, "SAT instance mismatch: {0} (local) vs {1} (remote)",
          new Object[] { hash, sid.getInstanceHash() });
      return false;
    }
    return true;
  }

  private static void startVisualisationGui(
      Mediator mediator,
      ConsumerConfig config,
      int variableAmount
  ) {
    VisualizationController visController = new VisualizationController(
        mediator,
        config,
        variableAmount
    );
    mediator.registerFrameAction(visController::onClauseUpdate);
    VisualizationStarter.setVisualizationController(visController);
    GuiUtils.launch(VisualizationStarter.class);
  }

  private record GlComponents(Graph graph, IntUnaryOperator nodeMapping, VideoController controller) {

  }

  private record InitialGraphInfo(int variables, ClauseUpdate[] clauses) {

  }
}
