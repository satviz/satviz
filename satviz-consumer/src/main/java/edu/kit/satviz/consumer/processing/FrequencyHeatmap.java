package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/**
 * A specialisation of {@link Heatmap}.
 * <br>This implementation considers the frequency of each variable in the {@code n} most recently
 * processed clauses and assigns heat values based on a variable's portion of occurrence.
 */
public class FrequencyHeatmap extends Heatmap {

  private final Map<Integer, Integer> frequencies;

  private HeatStrategy strategy;

  /**
   * Create a frequency-based heatmap with the given initial size.
   *
   * @param initialSize The amount of clauses to consider at a time.
   * @param strategy The strategy to use for heat value calculation.
   */
  public FrequencyHeatmap(int initialSize, HeatStrategy strategy) {
    super(initialSize);
    this.frequencies = new HashMap<>();
    this.strategy = strategy;
  }

  /**
   * The strategies for determining the heat value given a frequency {@code n}.
   */
  public enum HeatStrategy {
    /**
     * {@code n / maxFrequencyOfRecentVariables}.
     */
    MAX_FREQUENCY,

    /**
     * {@code n / currentHeatmapPopulationSize}.
     */
    SIZE
  }

  @Override
  public HeatUpdate process(ClauseUpdate[] updates, Graph graph, IntUnaryOperator nodeMapping) {
    int totalAmount = cursor;
    boolean full = false;
    for (ClauseUpdate update : updates) {
      Clause clause = update.clause();
      Clause previous = recentClauses[cursor];
      if (previous != null) {
        // if we encounter an existing element, the ring buffer is full and
        // the variables' frequencies need to be decremented
        full = true;
        decreaseFrequencies(previous);
      }
      recentClauses[cursor] = clause;
      increaseFrequencies(clause);
      totalAmount++;
      increaseCursor();
    }
    return populateUpdate(
        switch (strategy) {
          case MAX_FREQUENCY -> frequencies.values().stream().reduce(1, Math::max);
          case SIZE -> full ? recentClauses.length : totalAmount;
        },
        nodeMapping);
  }

  public HeatStrategy getStrategy() {
    return strategy;
  }

  public void setStrategy(HeatStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  protected void removeClause(Clause clause) {
    decreaseFrequencies(clause);
  }

  /* Calculate the updated heat values for each node based on its frequency and the total amount
   of nodes currently being updated. */
  private HeatUpdate populateUpdate(int totalAmount, IntUnaryOperator nodeMapping) {
    HeatUpdate update = new HeatUpdate();
    var iterator = frequencies.entrySet().iterator();
    while (iterator.hasNext()) {
      var entry = iterator.next();
      update.add(nodeMapping.applyAsInt(entry.getKey()), (float) entry.getValue() / totalAmount);
      if (entry.getValue() == 0) {
        iterator.remove();
      }
    }
    return update;
  }

  /* Increment the frequencies of the variables in a clause */
  private void increaseFrequencies(Clause clause) {
    for (int literal : clause.literals()) {
      frequencies.compute(Math.abs(literal), (k, v) -> v == null ? 1 : v + 1);
    }
  }

  /* Decrement the frequencies of the variables in a clause - not going lower than 0 */
  private void decreaseFrequencies(Clause clause) {
    for (int literal : clause.literals()) {
      int variable = Math.abs(literal);
      Integer val = frequencies.get(variable);
      if (val == null) {
        frequencies.put(variable, 0);
      } else if (val > 0) {
        frequencies.put(variable, val - 1);
      }
    }
  }

  @Override
  public void reset() {
    super.reset();
    frequencies.clear();
  }
}
