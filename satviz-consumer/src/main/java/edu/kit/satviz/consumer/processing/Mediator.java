package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.network.ConsumerConnectionListener;
import edu.kit.satviz.network.ProducerId;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import javafx.scene.paint.Color;

public class Mediator implements ConsumerConnectionListener {

  private Mediator(ClauseCoordinator coordinator, Heatmap heatmap, VariableInteractionGraph vig) {

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

  }

  public void pauseOrContinueRecording() {

  }

  public void pauseOrContinueVisualization() {

  }

  public void relayout() {

  }

  public void seekToUpdate(long index) {

  }

  public long currentUpdate() {
    return 0;
  }

  public long numberOfUpdates() {
    return 0;
  }

  public void quit() {

  }

  @Override
  public void onClauseUpdate(ProducerId pid, ClauseUpdate c) {

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
