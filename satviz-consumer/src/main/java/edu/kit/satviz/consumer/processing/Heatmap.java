package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Heatmap implements ClauseUpdateProcessor {

  private final Map<Integer, Integer> frequencies;
  private Clause[] recentClauses;
  private int cursor;

  public Heatmap(int initialSize) {
    this.frequencies = new HashMap<>(initialSize);
    this.recentClauses = new Clause[initialSize];
    this.cursor = 0;
  }

  public void setHeatmapSize(int heatmapSize) {
    this.frequencies.clear();
    this.recentClauses = new Clause[heatmapSize];
    this.cursor = 0;
  }

  public int getHeatmapSize() {
    return recentClauses.length;
  }

  @Override
  public HeatUpdate process(ClauseUpdate[] updates, Graph graph) {
    HeatUpdate update = new HeatUpdate();
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
    for (var entry : frequencies.entrySet()) {
      update.add(entry.getKey() - 1, (float) entry.getValue() / totalAmount);
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
      if (val != null) {
        if (val == 1) {
          frequencies.remove(variable);
        } else {
          frequencies.put(variable, val - 1);
        }
      }
    }
  }

  @Override
  public void deserialize(InputStream in) {
    reset();
  }

  @Override
  public void reset() {
    setHeatmapSize(recentClauses.length);
  }
}
