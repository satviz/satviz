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
      int srcPos = cursor - newSize;
      System.arraycopy(recentClauses, srcPos, temp, 0, newSize);
      for (int i = 0; i < srcPos; i++) {
        decreaseFrequencies(recentClauses[i]);
      }
      for (int i = cursor; i < prevSize; i++) {
        decreaseFrequencies(recentClauses[i]);
      }
      cursor = 0;
    } else {
      System.arraycopy(recentClauses, 0, temp, 0, cursor);
      int remaining = newSize - cursor;
      System.arraycopy(recentClauses, prevSize - remaining, temp, cursor, remaining);
      for (int i = cursor; i < prevSize - remaining; i++) {
        decreaseFrequencies(recentClauses[i]);
      }
    }
    recentClauses = temp;
  }

  private void grow(int prevSize, int newSize) {
    Clause[] temp = new Clause[newSize];
    System.arraycopy(recentClauses, 0, temp, 0, cursor);
    int remaining = prevSize - cursor;
    System.arraycopy(recentClauses, cursor, temp, newSize - remaining, remaining);
    recentClauses = temp;
  }

  public int getHeatmapSize() {
    return recentClauses.length;
  }

  @Override
  public HeatUpdate process(ClauseUpdate[] updates, Graph graph) {
    boolean full = false;
    for (int i = 0; i < updates.length; i++) {
      Clause clause = updates[i].clause();
      cursor = (cursor + i) % recentClauses.length;
      Clause previous = recentClauses[cursor];
      if (previous != null) {
        full = true;
        decreaseFrequencies(previous);
      }
      recentClauses[cursor] = clause;
      increaseFrequencies(clause);
    }

    int totalAmount = full ? recentClauses.length : cursor;
    return populateUpdate(totalAmount);
  }

  private HeatUpdate populateUpdate(int totalAmount) {
    HeatUpdate update = new HeatUpdate();
    var iterator = frequencies.entrySet().iterator();
    while (iterator.hasNext()) {
      var entry = iterator.next();
      update.add(entry.getKey() - 1, (float) entry.getValue() / totalAmount);
      if (entry.getValue() == 0) {
        iterator.remove();
      }
    }
    return update;
  }

  private void increaseFrequencies(Clause clause) {
    for (int literal : clause.literals()) {
      frequencies.compute(Math.abs(literal), (k, v) -> v == null ? 1 : v + 1);
    }
  }

  private void decreaseFrequencies(Clause clause) {
    for (int literal : clause.literals()) {
      int variable = Math.abs(literal);
      Integer val = frequencies.get(variable);
      if (val != null && val > 0) {
        frequencies.put(variable, val - 1);
      }
    }
  }

  @Override
  public void deserialize(InputStream in) {

  }

  @Override
  public void reset() {
    this.frequencies.clear();
    this.recentClauses = new Clause[recentClauses.length];
    this.cursor = 0;
  }
}
