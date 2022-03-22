package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.config.Theme;
import edu.kit.satviz.consumer.display.VideoController;
import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.network.pub.ConsumerConnectionListener;
import edu.kit.satviz.network.pub.ProducerId;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;

public class Mediator implements ConsumerConnectionListener, AutoCloseable {

  private static final Logger logger = Logger.getLogger("Mediator");

  private final Object renderLock = new Object();
  private final Graph graph;
  private final VideoController videoController;
  private final ClauseCoordinator coordinator;
  private final Heatmap heatmap;
  private final VariableInteractionGraph vig;
  private final ConsumerConfig config;
  private final ScheduledExecutorService glScheduler;
  private final long period;
  private final Queue<Runnable> taskQueue;
  private final List<Runnable> closeActions;
  private final List<Runnable> frameActions;
  private final Theme theme;

  private boolean recording;
  private boolean recordingPaused;
  private int recordedVideos;
  private int clauseCount;
  private volatile Future<?> currentRender;
  private boolean isRendering;

  private volatile boolean visualizationPaused;
  private volatile int clausesPerAdvance;
  private volatile int snapshotPeriod;

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
    this.isRendering = false;
    this.clausesPerAdvance = config.getBufferSize();
    this.period = config.getPeriod();
    this.clauseCount = 0;
    this.snapshotPeriod = clausesPerAdvance * 500;
    this.taskQueue = new LinkedBlockingQueue<>();
    this.closeActions = new CopyOnWriteArrayList<>();
    this.frameActions = new CopyOnWriteArrayList<>();
    this.theme = config.getTheme();
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
    theme.setColdColor(color);
    taskQueue.offer(() -> videoController.applyTheme(theme));
  }

  public void updateHeatmapHotColor(Color color) {
    theme.setHotColor(color);
    taskQueue.offer(() -> videoController.applyTheme(theme));
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
    synchronized (renderLock) {
      isRendering = true;
      currentRender = glScheduler.submit(this::render);
    }
    visualizationPaused = false;
  }

  public void pauseOrContinueVisualization() {
    visualizationPaused = !visualizationPaused;
  }

  public void relayout() {
    taskQueue.offer(graph::recalculateLayout);
    resetCamera();
  }

  public void resetCamera() {
    taskQueue.offer(videoController::resetCamera);
  }

  public void seekToUpdate(long index) {
    taskQueue.offer(() -> {
      try {
        coordinator.seekToUpdate(index);
      } catch (Throwable e) { // TODO: 10/02/2022
        e.printStackTrace();
      }
    });
    resetCamera();
  }

  public long currentUpdate() {
    return coordinator.currentUpdate();
  }

  public long numberOfUpdates() {
    return coordinator.totalUpdateCount();
  }

  public void registerCloseAction(Runnable closeAction) {
    closeActions.add(closeAction);
  }

  public void registerFrameAction(Runnable frameAction) {
    frameActions.add(frameAction);
  }

  private void render() {
    try {
      long start = System.currentTimeMillis();
      if (!visualizationPaused) {
        clauseCount += coordinator.advanceVisualization(clausesPerAdvance);
      }
      videoController.nextFrame();
      while (!taskQueue.isEmpty()) {
        taskQueue.poll().run();
      }

      frameActions.forEach(Runnable::run);

      if (clauseCount >= snapshotPeriod) {
        coordinator.takeSnapshot();
        clauseCount = 0;
      }
      long end = System.currentTimeMillis();
      // mutual exclusion for isRendering: only schedule next frame if isRendering=true
      synchronized (renderLock) {
        if (isRendering) {
          // next frame in frame period - time it took to render this frame
          currentRender = glScheduler.schedule(
              this::render, Math.max(0, period - (end - start)), TimeUnit.MILLISECONDS
          );
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onClauseUpdate(ProducerId pid, ClauseUpdate c) {
    try {
      coordinator.addClauseUpdate(c);
    } catch (IOException e) { // TODO: 10/02/2022
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onTerminateSolved(ProducerId pid, SatAssignment assign) {
    logger.info("Connection terminated - Result: satisfiable");
  }

  @Override
  public void onTerminateRefuted(ProducerId pid) {
    logger.info("Connection terminated - Result: not satisfiable");
  }

  @Override
  public void onTerminateOtherwise(ProducerId pid, String reason) {
    logger.log(Level.WARNING, "Connection terminated: {0}", reason);
    try {
      close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws Exception {
    // this guarantees that isRendering is set to false before
    // or after a new frame rendering task has been set
    synchronized (renderLock) {
      isRendering = false;
    }
    // wait for current frame to end
    if (currentRender != null) {
      currentRender.get();
    }
    boolean isRecording = recording;

    // close all opengl related stuff
    glScheduler.submit(() -> {
      if (isRecording) {
        videoController.finishRecording();
      }
      videoController.close();
      graph.close();
    }).get();
    glScheduler.shutdown();
    closeActions.forEach(Runnable::run);
    coordinator.close();
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
