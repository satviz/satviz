package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@code ClauseUpdateProcessor} that realises a heatmap of variables (nodes).
 * <br>The heatmap considers the frequency of each variable in the {@code n} most recently
 * processed clauses and calculates the variable's portion of occurrence based on that.
 *
 * @see HeatUpdate
 */
public class Heatmap implements ClauseUpdateProcessor {

  private final Map<Integer, Integer> frequencies;
  private Clause[] recentClauses;
  private int cursor;

  /**
   * Create a heatmap with the given initial size.
   *
   * @param initialSize The amount of clauses to consider at a time.
   */
  public Heatmap(int initialSize) {
    this.frequencies = new HashMap<>(initialSize);
    this.recentClauses = new Clause[initialSize];
    this.cursor = 0;
  }

  /**
   * (Re)set the heatmap size.<br>
   * This will reset the internal state of
   *
   * @param heatmapSize The
   */
  public void setHeatmapSize(int heatmapSize) {
    int prevSize = recentClauses.length;
    if (heatmapSize < prevSize) {
      shrink(prevSize, heatmapSize);
    } else if (heatmapSize > prevSize) {
      grow(prevSize, heatmapSize);
    }
  }

  private void shrink(int prevSize, int newSize) {
    Clause[] temp = new Clause[newSize];
    if (cursor >= newSize) {
      /* shrinking, case "all remaining elements are between 0 and cursor":
         [1, 2, ..., s, ..., >c, c+1, ..., n] => [>s, ..., c-1] */
      int srcPos = cursor - newSize;
      System.arraycopy(recentClauses, srcPos, temp, 0, newSize);
      /* Decrement [1, 2, ..., s-1] */
      for (int i = 0; i < srcPos; i++) {
        decreaseFrequencies(recentClauses[i]);
      }
      /* Decrement [c, ..., n] */
      for (int i = cursor; i < prevSize; i++) {
        decreaseFrequencies(recentClauses[i]);
      }
      cursor = 0;
    } else {
      /* shrinking, case "0 to cursor + elements from the end are included":
         [1, 2, ..., >c, c+1, ..., s, s+1, ..., n] => [1, 2, ..., >s, s+1, ..., n] */
      System.arraycopy(recentClauses, 0, temp, 0, cursor);
      int remaining = newSize - cursor;
      System.arraycopy(recentClauses, prevSize - remaining, temp, cursor, remaining);
      /* Decrement [c, ..., s-1] */
      for (int i = cursor; i < prevSize - remaining; i++) {
        decreaseFrequencies(recentClauses[i]);
      }
    }
    recentClauses = temp;
  }

  /* Growing the ring buffer:
     [1, 2, ..., >c, c+1, ..., n] => [1, 2, ..., >c, null, null, ..., c+1, ..., n] */
  private void grow(int prevSize, int newSize) {
    Clause[] temp = new Clause[newSize];
    System.arraycopy(recentClauses, 0, temp, 0, cursor);
    int remaining = prevSize - cursor;
    System.arraycopy(recentClauses, cursor, temp, newSize - remaining, remaining);
    recentClauses = temp;
  }

  /**
   * Get the heatmap size currently being used.
   *
   * @return the heatmap window size.
   */
  public int getHeatmapSize() {
    return recentClauses.length;
  }

  @Override
  public HeatUpdate process(ClauseUpdate[] updates, Graph graph) {
    int totalAmount = 0;
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
      totalAmount = ++cursor;
      cursor %= recentClauses.length;
    }
    return populateUpdate(full ? recentClauses.length : totalAmount);
  }

  /* Calculate the updated heat values for each node based on its frequency and the total amount
     of nodes currently being updated. */
  private HeatUpdate populateUpdate(int totalAmount) {
    HeatUpdate update = new HeatUpdate();
    var iterator = frequencies.entrySet().iterator();
    while (iterator.hasNext()) {
      var entry = iterator.next();
      // key - 1 here to convert between 1-indexed variables and 0-indexed graph nodes
      update.add(entry.getKey() - 1, (float) entry.getValue() / totalAmount);
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
      if (val != null && val > 0) {
        frequencies.put(variable, val - 1);
      }
    }
  }

  /* The heatmap currently doesn't serialise anything, so when a different state is loaded
     it simply resets and starts from scratch. */
  @Override
  public void deserialize(InputStream in) {
    reset();
  }

  @Override
  public void reset() {
    this.frequencies.clear();
    this.recentClauses = new Clause[recentClauses.length];
    this.cursor = 0;
  }
}
