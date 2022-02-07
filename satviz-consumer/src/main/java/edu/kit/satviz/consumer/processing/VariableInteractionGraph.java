package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.WeightUpdate;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.Serializer;

public class VariableInteractionGraph implements ClauseUpdateProcessor {

  @Override
  public WeightUpdate process(ClauseUpdate[] clauseUpdates, Graph graph) {
    return null;
  }

  @Override
  public Serializer<VariableInteractionGraph> serializer() {
    return null;
  }

}
