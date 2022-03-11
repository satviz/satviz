package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.display.VideoController;
import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.network.ConsumerConnection;
import edu.kit.satviz.network.ConsumerConnectionListener;
import edu.kit.satviz.network.ProducerId;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.scene.paint.Color;

public class Mediator implements ConsumerConnectionListener {

  private final Graph graph;
  private final VideoController videoController;
  private final ClauseCoordinator coordinator;
  private final Heatmap heatmap;
  private final VariableInteractionGraph vig;
  private final ConsumerConfig config;
  private final ConsumerConnection connection;
  private final ScheduledExecutorService glScheduler;
  private final long period;
  private final Queue<Runnable> taskQueue;

  private boolean recording;
  private boolean recordingPaused;
  private volatile boolean visualizationPaused;
  private int recordedVideos;
  private volatile int clausesPerAdvance;
  private volatile int snapshotPeriod;
  private int clauseCount;

  private Mediator(
      ScheduledExecutorService glScheduler,
      Graph graph,
      VideoController controller,
      ClauseCoordinator coordinator,
      Heatmap heatmap,
      VariableInteractionGraph vig,
      ConsumerConnection connection,
      ConsumerConfig config
  ) {
    this.glScheduler = glScheduler;
    this.graph = graph;
    this.videoController = controller;
    this.coordinator = coordinator;
    this.heatmap = heatmap;
    this.vig = vig;
    this.connection = connection;
    this.config = config;
    this.recording = false;
    this.recordedVideos = 0;
    this.recordingPaused = false;
    this.visualizationPaused = true;
    this.clausesPerAdvance = config.getBufferSize();
    this.period = config.getPeriod();
    this.clauseCount = 0;
    this.snapshotPeriod = clausesPerAdvance * 500;
    this.taskQueue = new LinkedBlockingQueue<>();
    //System.out.println("Period: " + period + ", buffer: " + clausesPerAdvance);
    coordinator.addProcessor(heatmap);
    coordinator.addProcessor(vig);
  }

  public void updateWeightFactor(WeightFactor factor) {
    vig.setWeightFactor(factor);
  }

  public void updateWindowSize(int windowSize) {
    taskQueue.offer(() -> heatmap.setHeatmapSize(windowSize));
  }

  public void updateHeatmapColdColor(Color color) {
    // TODO: 19/02/2022
  }

  public void updateHeatmapHotColor(Color color) {
    // TODO: 19/02/2022
  }

  public void setClausesPerAdvance(int clausesPerAdvance) {
    this.clausesPerAdvance = clausesPerAdvance;
  }

  public long getPeriod() {
    return period;
  }

  public int getClausesPerAdvance() {
    return clausesPerAdvance;
  }

  public void highlightVariable(int variable) {
    // TODO: 19/02/2022
  }

  public void clearHighlightVariable() {
    // TODO: 19/02/2022
  }

  public void screenshot() {
    // TODO: 19/02/2022
  }

  public void startOrStopRecording() {
    if (recording) {
      if (recordingPaused) {
        taskQueue.offer(videoController::resumeRecording);
      }
      taskQueue.offer(videoController::finishRecording);
    } else {
      String filename = config.getVideoTemplatePath()
          .replace("{}", String.valueOf(++recordedVideos));
      taskQueue.offer(() -> videoController.startRecording(filename, "theora"));
    }
    recordingPaused = false;
    recording = !recording;
  }

  public void pauseOrContinueRecording() {
    if (recording) {
      if (recordingPaused) {
        taskQueue.offer(videoController::resumeRecording);
      } else {
        taskQueue.offer(videoController::stopRecording);
      }
      recordingPaused = !recordingPaused;
    }
  }

  public void startRendering() {
    glScheduler.scheduleAtFixedRate(
        this::render,
        0,
        period,
        TimeUnit.MILLISECONDS
    );
    visualizationPaused = false;
  }

  public void pauseOrContinueVisualization() {
    visualizationPaused = !visualizationPaused;
  }

  public void relayout() {
    taskQueue.offer(graph::recalculateLayout);
  }

  public void seekToUpdate(long index) {
    taskQueue.offer(() -> {
      try {
        coordinator.seekToUpdate(index);
      } catch (Throwable e) { // TODO: 10/02/2022
        e.printStackTrace();
      }
    });
  }

  public long currentUpdate() {
    return coordinator.currentUpdate();
  }

  public long numberOfUpdates() {
    return coordinator.totalUpdateCount();
  }

  public void quit() {
    taskQueue.offer(() -> {
      try {
        videoController.close();
        connection.stop();
        coordinator.close();
        glScheduler.shutdown();
      } catch (Throwable e) {
        e.printStackTrace();
      }
    });
  }

  private void render() {
    try {
      if (!visualizationPaused) {
        clauseCount += coordinator.advanceVisualization(clausesPerAdvance);
      }
      videoController.nextFrame();
      while (!taskQueue.isEmpty()) {
        taskQueue.poll().run();
      }
      if (clauseCount >= snapshotPeriod) {
        coordinator.takeSnapshot();
        clauseCount = 0;
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onClauseUpdate(ProducerId pid, ClauseUpdate c) {
    //System.out.println("Clause " + c);
    try {
      coordinator.addClauseUpdate(c);
    } catch (IOException e) { // TODO: 10/02/2022
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onTerminateSolved(ProducerId pid, SatAssignment sol) {
    advanceRestAndShutdown();
  }

  @Override
  public void onTerminateRefuted(ProducerId pid) {
    advanceRestAndShutdown();
  }

  @Override
  public void onTerminateOtherwise(ProducerId pid, String reason) {
    advanceRestAndShutdown();
  }

  private void advanceRestAndShutdown() {
    if (true) { // TODO: 22/02/2022 remove
      return;
    }
    System.out.println("shutdown");
    glScheduler.shutdown();
    int updateAmount = (int) (coordinator.totalUpdateCount() - coordinator.currentUpdate());
    try {
      coordinator.advanceVisualization(updateAmount);
    } catch (IOException | SerializationException e) { // TODO: 19/02/2022
      throw new RuntimeException(e);
    }
  }

  public static class MediatorBuilder {
    private Graph graph;
    private VideoController controller;
    private ClauseCoordinator coordinator;
    private Heatmap heatmap;
    private VariableInteractionGraph vig;
    private ConsumerConnection connection;
    private ConsumerConfig config;
    private ScheduledExecutorService glScheduler;

    public MediatorBuilder setGraph(Graph graph) {
      this.graph = graph;
      return this;
    }

    public MediatorBuilder setController(VideoController controller) {
      this.controller = controller;
      return this;
    }

    public MediatorBuilder setCoordinator(ClauseCoordinator coordinator) {
      this.coordinator = coordinator;
      return this;
    }

    public MediatorBuilder setHeatmap(Heatmap heatmap) {
      this.heatmap = heatmap;
      return this;
    }

    public MediatorBuilder setVig(VariableInteractionGraph vig) {
      this.vig = vig;
      return this;
    }

    public MediatorBuilder setGlScheduler(ScheduledExecutorService scheduler) {
      this.glScheduler = scheduler;
      return this;
    }

    public MediatorBuilder setConfig(ConsumerConfig config) {
      this.config = config;
      return this;
    }

    public MediatorBuilder setConnection(ConsumerConnection connection) {
      this.connection = connection;
      return this;
    }

    public Mediator createMediator() {
      return new Mediator(
          glScheduler,
          graph,
          controller,
          coordinator,
          heatmap,
          vig,
          connection,
          config
      );
    }
  }

}
