package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.display.VideoController;
import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.network.ConsumerConnectionListener;
import edu.kit.satviz.network.ProducerId;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.scene.paint.Color;

public class Mediator implements ConsumerConnectionListener {

  private final Graph graph;
  private final VideoController videoController;
  private final ClauseCoordinator coordinator;
  private final Heatmap heatmap;
  private final VariableInteractionGraph vig;
  private final ConsumerConfig config;
  private final ScheduledExecutorService glScheduler;

  private boolean recording;
  private boolean recordingPaused;
  private volatile boolean visualizationPaused;
  private int recordedVideos;
  private volatile int clausesPerAdvance;
  private volatile long period;

  private Mediator(
      ScheduledExecutorService glScheduler,
      Graph graph,
      VideoController controller,
      ClauseCoordinator coordinator,
      Heatmap heatmap,
      VariableInteractionGraph vig,
      ConsumerConfig config
  ) {
    this.glScheduler = glScheduler;
    this.graph = graph;
    this.videoController = controller;
    this.coordinator = coordinator;
    this.heatmap = heatmap;
    this.vig = vig;
    this.config = config;
    this.recording = false;
    this.recordedVideos = 0;
    this.recordingPaused = false;
    this.visualizationPaused = true;
    this.clausesPerAdvance = config.getBufferSize();
    this.period = config.getPeriod();
    //System.out.println("Period: " + period + ", buffer: " + clausesPerAdvance);
    coordinator.addProcessor(heatmap);
    coordinator.addProcessor(vig);
  }

  public void updateWeightFactor(WeightFactor factor) {
    vig.setWeightFactor(factor);
  }

  public void updateWindowSize(int windowSize) {
    glScheduler.submit(() -> heatmap.setHeatmapSize(windowSize));
  }

  public void updateHeatmapColdColor(Color color) {
    // TODO: 19/02/2022
  }

  public void updateHeatmapHotColor(Color color) {
    // TODO: 19/02/2022
  }

  public void setPeriod(long period) {
    this.period = period;
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
      glScheduler.submit(videoController::finishRecording);
    } else {
      String filename = config.getVideoTemplatePath()
          .replace("{}", String.valueOf(++recordedVideos));
      glScheduler.submit(() -> {
        videoController.startRecording(filename, "theora");
        System.out.println("Recording started");
      });
    }
    recordingPaused = false;
    recording = !recording;
  }

  public void pauseOrContinueRecording() {
    if (recording) {
      if (recordingPaused) {
        glScheduler.submit(videoController::resumeRecording);
      } else {
        glScheduler.submit(videoController::stopRecording);
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
    glScheduler.submit(graph::recalculateLayout);
  }

  public void seekToUpdate(long index) {
    glScheduler.submit(() -> {
      try {
        coordinator.seekToUpdate(index);
      } catch (IOException | SerializationException e) { // TODO: 10/02/2022
        e.printStackTrace();
        glScheduler.shutdown();
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
    videoController.close();
  }

  private void render() {
    try {
      //System.out.println("Advance call");
      if (!visualizationPaused) {
        coordinator.advanceVisualization(clausesPerAdvance);
      }
      //System.out.println("Post advance");
      videoController.nextFrame();
      //System.out.println("Post nextframe");
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

    public Mediator createMediator() {
      return new Mediator(
          glScheduler,
          graph,
          controller,
          coordinator,
          heatmap,
          vig,
          config
      );
    }
  }

}
