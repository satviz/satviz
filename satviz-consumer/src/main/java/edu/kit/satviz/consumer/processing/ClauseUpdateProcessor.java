package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.GraphUpdate;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.Serializer;

public interface ClauseUpdateProcessor {

  GraphUpdate process(ClauseUpdate[] clauseUpdates, Graph graph);

  Serializer<? extends ClauseUpdateProcessor> serializer();


}
