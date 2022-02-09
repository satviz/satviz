package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.GraphUpdate;
import edu.kit.satviz.sat.ClauseUpdate;

import java.io.InputStream;
import java.io.OutputStream;

public interface ClauseUpdateProcessor {

  GraphUpdate process(ClauseUpdate[] clauseUpdates, Graph graph);

  default void serialize(OutputStream out) {

  }

  default void deserialize(InputStream in) {

  }

  default void reset() {

  }

}
