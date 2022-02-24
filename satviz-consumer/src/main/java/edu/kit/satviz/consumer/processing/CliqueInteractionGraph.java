package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.graph.WeightUpdate;

public class CliqueInteractionGraph extends VariableInteractionGraph {

  /**
   * Create a clique-based VIG with the given initial {@code WeightFactor}.
   *
   * @param weightFactor An instance of the {@code WeightFactor} enum.
   */
  public CliqueInteractionGraph(WeightFactor weightFactor) {
    super(weightFactor);
  }

  @Override
  protected void process(WeightUpdate weightUpdate, int[] variables, float weight) {
    for (int i = 0; i < variables.length; i++) {
      for (int j = i + 1; j < variables.length; j++) {
        weightUpdate.add(variables[i] - 1, variables[j] - 1, weight);
      }
    }
  }
}
