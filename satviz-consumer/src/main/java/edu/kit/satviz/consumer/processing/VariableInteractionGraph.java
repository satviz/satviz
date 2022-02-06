package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.WeightUpdate;
import edu.kit.satviz.sat.ClauseUpdate;

import java.io.InputStream;
import java.io.OutputStream;

public class VariableInteractionGraph implements ClauseUpdateProcessor {

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
