package edu.kit.satviz.sat;

/**
 * This record adds additional information to the Clause record.<br>
 * One can differentiate between different types of clauses (in the <code>Type</code> enum).
 *
 * @author quorty
 */
public record ClauseUpdate(Clause clause, Type type) {
  /**
   * This enum holds possible clause update types.
   */
  public enum Type {
    ADD,
    REMOVE
  }
}
