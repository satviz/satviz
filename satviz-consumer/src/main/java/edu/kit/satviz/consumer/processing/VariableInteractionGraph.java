package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.WeightUpdate;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.StringSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VariableInteractionGraph implements ClauseUpdateProcessor {

  private WeightFactor weightFactor;
  private final StringSerializer serializer;

  public VariableInteractionGraph(WeightFactor weightFactor) {
    this.weightFactor = weightFactor;
    this.serializer = new StringSerializer();
  }

  public void setWeightFactor(WeightFactor weightFactor) {
    this.weightFactor = weightFactor;
  }

  public WeightFactor getWeightFactor() {
    return weightFactor;
  }

  @Override
  public WeightUpdate process(ClauseUpdate[] clauseUpdates, Graph graph) {
    WeightUpdate weightUpdate = new WeightUpdate();
    int[] literals;
    float weight;
    for (ClauseUpdate clauseUpdate : clauseUpdates) {
      literals = clauseUpdate.clause().literals();
      weight = (float) weightFactor.apply(literals.length);
      weight = (clauseUpdate.type() == ClauseUpdate.Type.ADD) ? weight : -weight;
      for (int i = 0; i < literals.length; i++) {
        for (int j = i + 1; j < literals.length; j++) {
          weightUpdate.add(
              Math.abs(literals[i]) - 1,
              Math.abs(literals[j]) - 1,
              weight
          );
        }
      }
    }
    return weightUpdate;
  }

  @Override
  public void serialize(OutputStream out) {
    try {
      serializer.serialize(weightFactor.name(), out);
    } catch (IOException | SerializationException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deserialize(InputStream in) {
    try {
      String weightFactorName = serializer.deserialize(in);
      weightFactor = WeightFactor.valueOf(weightFactorName);
    } catch (IOException | SerializationException e) {
      e.printStackTrace();
    }
  }

}
