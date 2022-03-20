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
import java.util.function.IntUnaryOperator;

/**
 * An implementation of {@code ClauseUpdateProcessor} that realises weight-changes for a
 * variable interaction graph.<br>
 * The VIG considers the clause length and {@code WeightFactor} when processing a
 * {@code ClauseUpdate}.
 *
 * @see WeightUpdate
 */
public abstract class VariableInteractionGraph implements ClauseUpdateProcessor {

  public static final VariableInteractionGraphImplementation DEFAULT_IMPLEMENTATION =
      VariableInteractionGraphImplementation.RING;

  private WeightFactor weightFactor;
  private final StringSerializer serializer;

  /**
   * Create a VIG with the given initial {@code WeightFactor}.
   *
   * @param weightFactor An instance of the {@code WeightFactor} enum.
   */
  protected VariableInteractionGraph(WeightFactor weightFactor) {
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
  public WeightUpdate process(
      ClauseUpdate[] clauseUpdates, Graph graph, IntUnaryOperator nodeMapping
  ) {
    WeightUpdate weightUpdate = new WeightUpdate();
    for (ClauseUpdate clauseUpdate : clauseUpdates) {
      int[] literals = clauseUpdate.clause().literals();
      if (literals.length < 2) {
        continue;
      }
      for (int i = 0; i < literals.length; i++) {
        literals[i] = Math.abs(literals[i]);
      }

      float weight = (float) weightFactor.apply(literals.length);
      weight = (clauseUpdate.type() == ClauseUpdate.Type.ADD) ? weight : -weight;
      process(weightUpdate, literals, weight, nodeMapping);
    }
    return weightUpdate;
  }

  protected abstract void process(
      WeightUpdate weightUpdate, int[] variables, float weight, IntUnaryOperator nodeMapping
  );

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
