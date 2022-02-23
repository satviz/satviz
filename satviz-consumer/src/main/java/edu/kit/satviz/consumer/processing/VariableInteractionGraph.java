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
import java.util.Arrays;

/**
 * An implementation of {@code ClauseUpdateProcessor} that realises weight-changes for a
 * variable interaction graph.<br>
 * The VIG considers the clause length and {@code WeightFactor} when processing a
 * {@code ClauseUpdate}.
 *
 * @see WeightUpdate
 */
public class VariableInteractionGraph implements ClauseUpdateProcessor {

  private WeightFactor weightFactor;
  private final StringSerializer serializer;

  /**
   * Create a VIG with the given initial {@code WeightFactor}.
   *
   * @param weightFactor An instance of the {@code WeightFactor} enum.
   */
  public VariableInteractionGraph(WeightFactor weightFactor) {
    this.weightFactor = weightFactor;
    this.serializer = new StringSerializer();
  }

  /**
   * This setter-method sets the {@code WeightFactor} for this processor.
   *
   * @param weightFactor An instance of the {@code WeightFactor} enum.
   */
  public void setWeightFactor(WeightFactor weightFactor) {
    this.weightFactor = weightFactor;
  }

  /**
   * This getter-method returns the {@code WeightFactor}, which is currently used for processing.
   *
   * @return An instance of the {@code WeightFactor} enum.
   */
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
      if (literals.length == 0) {
        continue;
      }
      for (int i = 0; i < literals.length; i++) {
        literals[i] = Math.abs(literals[i]);
      }
      Arrays.sort(literals);
      weight = (float) weightFactor.apply(literals.length);
      weight = (clauseUpdate.type() == ClauseUpdate.Type.ADD) ? weight : -weight;
      for (int i = 0; i < literals.length - 1; i++) {
        weightUpdate.add(
            literals[i] - 1,
            literals[i + 1] - 1,
            weight
        );
      }
      weightUpdate.add(literals[0] - 1, literals[literals.length - 1] - 1, weight);
    }
    return weightUpdate;
    /*WeightUpdate weightUpdate = new WeightUpdate();
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
    return weightUpdate;*/
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
