package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.display.VideoController;
import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.network.ConsumerConnectionListener;
import edu.kit.satviz.network.ProducerId;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
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

  private boolean recording;
  private boolean recordingPaused;

  private final ScheduledExecutorService advanceScheduler;
  private ScheduledFuture<?> currentTask;
  private volatile boolean stop;
  private volatile int clausesPerAdvance;
  private volatile long period;

  public Mediator(
      Graph graph,
      VideoController controller,
      ClauseCoordinator coordinator,
      Heatmap heatmap,
      VariableInteractionGraph vig,
      int clausesPerAdvance,
      long period
  ) {
    this.graph = graph;
    this.videoController = controller;
    this.coordinator = coordinator;
    this.heatmap = heatmap;
    this.vig = vig;
    this.recording = false;
    this.recordingPaused = false;
    this.advanceScheduler = Executors.newSingleThreadScheduledExecutor();
    this.currentTask = null;
    this.clausesPerAdvance = clausesPerAdvance;
    this.period = period;
    coordinator.addProcessor(heatmap);
    coordinator.addProcessor(vig);
  }

  public void updateWeightFactor(WeightFactor factor) {
    vig.setWeightFactor(factor);
  }

  public void updateWindowSize(int windowSize) {
    heatmap.setHeatmapSize(windowSize);
  }

  public void updateHeatmapColdColor(Color color) {

  }

  public void updateHeatmapHotColor(Color color) {

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

  }

  public void clearHighlightVariable() {

  }

  public void screenshot() {

  }

  public void startOrStopRecording() {
    if (recording) {
      if (!recordingPaused) {
        videoController.stopRecording();
      }
      videoController.finishRecording();
    } else {
      videoController.startRecording(null, null); // TODO: 10/02/2022
    }
  }

  public void pauseOrContinueRecording() {
    if (recording) {
      if (recordingPaused) {
        videoController.resumeRecording();
      } else {
        videoController.stopRecording();
      }
    }
  }

  public void pauseOrContinueVisualization() {
    if (currentTask == null) {
      currentTask = advanceScheduler.scheduleAtFixedRate(
          this::periodicallyAdvance,
          0,
          period,
          TimeUnit.MILLISECONDS
      );
    } else {
      currentTask.cancel(false);
      currentTask = null;
    }
  }

  public void relayout() {
    graph.recalculateLayout();
  }

  public void seekToUpdate(long index) {
    try {
      coordinator.seekToUpdate(index);
    } catch (IOException | SerializationException e) { // TODO: 10/02/2022
      e.printStackTrace();
    }
  }

  public long currentUpdate() {
    return coordinator.currentUpdate();
  }

  public long numberOfUpdates() {
    return coordinator.totalUpdateCount();
  }

  public void quit() {
    try {
      coordinator.close();
    } catch (IOException e) { // TODO: 10/02/2022
      e.printStackTrace();
    }
  }

  private void periodicallyAdvance() {
    if (!stop) {
      try {
        coordinator.advanceVisualization(clausesPerAdvance);
      } catch (IOException | SerializationException e) {
        e.printStackTrace();
        stop = true;
      }
    }
  }

  @Override
  public void onClauseUpdate(ProducerId pid, ClauseUpdate c) {
    try {
      coordinator.addClauseUpdate(c);
    } catch (IOException e) { // TODO: 10/02/2022
      e.printStackTrace();
    }
  }

  @Override
  public void onTerminateSolved(ProducerId pid, SatAssignment sol) {
    stop = true;
    advanceRestAndShutdown();
  }

  @Override
  public void onTerminateRefuted(ProducerId pid) {
    stop = true;
    advanceRestAndShutdown();
  }

  @Override
  public void onTerminateFailed(ProducerId pid, String reason) {
    stop = true;
    advanceRestAndShutdown();
  }

  private void advanceRestAndShutdown() {
    advanceScheduler.shutdown();
    int updateAmount = (int) (coordinator.totalUpdateCount() - coordinator.currentUpdate());
    try {
      coordinator.advanceVisualization(updateAmount);
    } catch (IOException | SerializationException e) {
      e.printStackTrace();
    }
  }

}
