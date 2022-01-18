package edu.kit.satviz.sat;

/**
 * This record is adds additional information to the Clause record.<br>
 * One can now differentiate between different types of clauses (in the <code>Type</code> enum).
 *
 * @author quorty
 */
public record ClauseUpdate(Clause clause, Type type) {
}
