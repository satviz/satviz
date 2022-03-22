package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntUnaryOperator;

/**
 * A specialisation of {@link Heatmap}.
 * <br>This implementation considers the <em>recency</em> of each variable in the {@code n}
 * most recently processed clauses and assigns heat values based on most recent appearances.
 */
public class RecencyHeatmap extends Heatmap {

  private final Set<Integer> setToZero;

  /**
   * Create a recency-based heatmap with the given initial size.
   *
   * @param initialSize The amount of clauses to consider at a time.
   */
  public RecencyHeatmap(int initialSize) {
    super(initialSize);
    this.setToZero = new HashSet<>();
  }

  @Override
  public HeatUpdate process(ClauseUpdate[] updates, Graph graph, IntUnaryOperator nodeMapping) {

    for (ClauseUpdate update : updates) {
      Clause previous = recentClauses[cursor];
      if (previous != null) {
        removeClause(previous);
      }
      recentClauses[cursor] = update.clause();
      increaseCursor();
    }

    int size = recentClauses.length;
    HeatUpdate update = new HeatUpdate();
    zeroPendingVariables(update, nodeMapping);
    for (int i = 1; i <= size; i++) {
      Clause subject = recentClauses[(i + cursor - 1) % size];
      if (subject == null) {
        continue;
      }
      for (int literal : subject.literals()) {
        update.add(nodeMapping.applyAsInt(literal), (float) i / size);
      }
    }
    return update;
  }

  private void zeroPendingVariables(HeatUpdate update, IntUnaryOperator nodeMapping) {
    for (int variable : setToZero) {
      update.add(nodeMapping.applyAsInt(variable), 0);
    }
    setToZero.clear();
  }

  @Override
  protected void removeClause(Clause clause) {
    for (int literal : clause.literals()) {
      setToZero.add(Math.abs(literal));
    }
  }
}
