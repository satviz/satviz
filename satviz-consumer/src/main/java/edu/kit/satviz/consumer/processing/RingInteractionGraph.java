package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.graph.WeightUpdate;
import java.util.Arrays;
import java.util.function.IntUnaryOperator;

public class RingInteractionGraph extends VariableInteractionGraph {

  /**
   * Create a ring-based VIG with the given initial {@code WeightFactor}.
   *
   * @param weightFactor An instance of the {@code WeightFactor} enum.
   */
  public RingInteractionGraph(WeightFactor weightFactor) {
    super(weightFactor);
  }

  @Override
  protected void process(
      WeightUpdate weightUpdate, int[] variables, float weight, IntUnaryOperator nodeMapping
  ) {
    Arrays.sort(variables);
    for (int i = 0; i < variables.length - 1; i++) {
      weightUpdate.add(
          nodeMapping.applyAsInt(variables[i]), nodeMapping.applyAsInt(variables[i + 1]), weight
      );
    }
    weightUpdate.add(
        nodeMapping.applyAsInt(variables[0]),
        nodeMapping.applyAsInt(variables[variables.length - 1]),
        weight
    );
  }
}
