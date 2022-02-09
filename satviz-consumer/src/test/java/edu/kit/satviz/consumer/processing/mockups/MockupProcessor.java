package edu.kit.satviz.consumer.processing.mockups;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.GraphUpdate;
import edu.kit.satviz.consumer.processing.ClauseUpdateProcessor;
import edu.kit.satviz.sat.ClauseUpdate;

import java.io.InputStream;
import java.io.OutputStream;

public class MockupProcessor implements ClauseUpdateProcessor {

  @Override
  public GraphUpdate process(ClauseUpdate[] clauseUpdates, Graph graph) {
    return new MockupUpdate();
  }

  @Override
  public void serialize(OutputStream out) {

  }

  @Override
  public void deserialize(InputStream in) {

  }
}
