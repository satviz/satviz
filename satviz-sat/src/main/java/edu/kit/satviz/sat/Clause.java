package edu.kit.satviz.sat;

import java.util.Arrays;

/**
 * A simple record class, that stores the literals of a clause in an integer array.<br>
 * For this to work, every variable of the SAT-Instance has to have an identifiable
 * <code>varID</code>. In case that variable within the clause is used as a positive literal,
 * the integer stored for it will be <code>varID</code>, and if it's used as a negative literal,
 * then the integer stored will be <code>-varID</code>.
 * If the clause contains <code>n</code> literals, the array size is exactly <code>n</code>.
 * No trailing zero is stored.
 */
public record Clause(int[] literals) {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Clause clause = (Clause) o;
    return Arrays.equals(literals, clause.literals);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(literals);
  }

  @Override
  public String toString() {
    return Arrays.toString(literals);
  }

}
