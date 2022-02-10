package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.display.VideoController;
import edu.kit.satviz.network.ConsumerConnectionListener;
import edu.kit.satviz.network.ProducerId;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import javafx.scene.paint.Color;

public class Mediator implements ConsumerConnectionListener {

  private final VideoController videoController;
  private final ClauseCoordinator coordinator;
  private final Heatmap heatmap;
  private final VariableInteractionGraph vig;

  private boolean recording;
  private boolean recordingPaused;

  public Mediator(
      VideoController controller,
      ClauseCoordinator coordinator,
      Heatmap heatmap,
      VariableInteractionGraph vig
  ) {
    this.videoController = controller;
    this.coordinator = coordinator;
    this.heatmap = heatmap;
    this.vig = vig;
    this.recording = false;
    this.recordingPaused = false;
    coordinator.addProcessor(heatmap);
    coordinator.addProcessor(vig);
  }

  public void updateWeightFactor(WeightFactor factor) {

  }

  public void updateWindowSize(int windowSize) {

  }

  public void updateHeatmapColdColor(Color color) {

  }

  public void updateHeatmapHotColor(Color color) {

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

  }

  public void relayout() {

  }

  public void seekToUpdate(long index) {
    try {
      coordinator.seekToUpdate(index);
    } catch (IOException e) { // TODO: 10/02/2022
      e.printStackTrace();
    } catch (SerializationException e) {
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

  }

  @Override
  public void onTerminateRefuted(ProducerId pid) {

  }

  @Override
  public void onTerminateFailed(ProducerId pid, String reason) {

  }

}
