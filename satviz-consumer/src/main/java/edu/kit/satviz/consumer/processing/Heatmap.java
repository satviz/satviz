package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.serial.ClauseSerializer;
import edu.kit.satviz.serial.IntSerializer;
import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * A kind of {@code ClauseUpdateProcessor} that realises a heatmap of variables (nodes).
 *
 * @see HeatUpdate
 */
public abstract class Heatmap implements ClauseUpdateProcessor {

  public static final HeatmapImplementation DEFAULT_IMPLEMENTATION = HeatmapImplementation.RECENCY;

  private static final IntSerializer intSerializer = new IntSerializer();
  private static final ClauseSerializer clauseSerializer = new ClauseSerializer();

  protected Clause[] recentClauses;
  protected int cursor;

  /**
   * Create a heatmap with the given initial size.
   *
   * @param initialSize The amount of clauses to consider at a time.
   */
  protected Heatmap(int initialSize) {
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
        removeClause(recentClauses[i]);
      }
      /* Decrement [c, ..., n] */
      for (int i = cursor; i < prevSize; i++) {
        Clause clause = recentClauses[i];
        if (clause != null) {
          removeClause(clause);
        }
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
        Clause clause = recentClauses[i];
        if (clause != null) {
          removeClause(recentClauses[i]);
        }
      }
    }
    recentClauses = temp;
  }

  /* Growing the ring buffer:
     [1, 2, ..., >c, c+1, ..., n] => [1, 2, ..., c-1, >null, null, ..., c, c+1, ..., n] */
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

  protected abstract void removeClause(Clause clause);

  protected final void increaseCursor() {
    cursor = (cursor + 1) % recentClauses.length;
  }

  /* Deserialising means resetting the entire heatmap,
     done by first resetting the clauses that were serialised, then the current clauses */
  @Override
  public void deserialize(InputStream in) throws IOException, SerializationException {
    int clauses = intSerializer.deserialize(in);
    for (int i = 0; i < clauses; i++) {
      removeClause(clauseSerializer.deserialize(in));
    }
    reset();
  }

  @Override
  public void serialize(OutputStream out) throws IOException {
    int savedClauses = (int) Arrays.stream(recentClauses).filter(Objects::nonNull).count();
    intSerializer.serialize(savedClauses, out);
    for (Clause clause : recentClauses) {
      if (clause != null) {
        clauseSerializer.serialize(clause, out);
      }
    }
  }

  @Override
  public void reset() {
    for (Clause clause : recentClauses) {
      if (clause != null) {
        removeClause(clause);
      }
    }
    this.recentClauses = new Clause[recentClauses.length];
    this.cursor = 0;
  }
}
