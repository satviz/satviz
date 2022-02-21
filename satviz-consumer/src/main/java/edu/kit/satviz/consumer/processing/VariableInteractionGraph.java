package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.WeightUpdate;
import edu.kit.satviz.sat.ClauseUpdate;

import java.io.InputStream;
import java.io.OutputStream;

public class VariableInteractionGraph implements ClauseUpdateProcessor {

  private WeightFactor weightFactor;

  public VariableInteractionGraph(WeightFactor weightFactor) {
    this.weightFactor = weightFactor;
  }

  public void setWeightFactor(WeightFactor weightFactor) {
    this.weightFactor = weightFactor;
  }

  public WeightFactor getWeightFactor() {
    return weightFactor;
  }

  @Override
  public WeightUpdate process(ClauseUpdate[] clauseUpdates, Graph graph) {
    return null;
  }

  @Override
  public void serialize(OutputStream out) {

  }

  @Override
  public void deserialize(InputStream in) {

  }

}
