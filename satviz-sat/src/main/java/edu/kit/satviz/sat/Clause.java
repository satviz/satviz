package edu.kit.satviz.sat;

/**
 * A simple record class, that stores the literals of a clause in an integer array.<br>
 * For this to work, every variable of the SAT-Instance has to have an identifiable <code>varID</code>.
 * In case that variable within the clause is used as a positive literal,
 * the integer stored for it will be <code>varID</code>, and if it's used as a negative literal,
 * then the integer stored will be <code>-varID</code>.
 *
 * @author quorty
 */
public record Clause(int[] literals) {
}
