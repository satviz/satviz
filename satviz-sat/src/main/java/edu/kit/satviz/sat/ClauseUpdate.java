package edu.kit.satviz.sat;

import java.util.NoSuchElementException;

/**
 * This record adds additional information to the Clause record.<br>
 * One can differentiate between different types of clauses (in the <code>Type</code> enum).
 */
public record ClauseUpdate(Clause clause, Type type) {

  /**
   * This method creates a new instance of the <code>ClauseUpdate</code> class from
   * a type and clause represented by integers.
   *
   * @param type An instance of the <code>Type</code> enum.
   * @param clause A clause represented by integers.
   * @return An instance of the <code>ClauseUpdate</code> class.
   */
  public static ClauseUpdate of(Type type, int... clause) {
    return new ClauseUpdate(new Clause(clause), type);
  }

  /**
   * This enum holds possible clause update types.
   */
  public enum Type {

    ADD((byte) 'a'),
    REMOVE((byte) 'd');

    private final byte id;

    Type(byte id) {
      this.id = id;
    }

    /**
     * Returns the byte-identifier of this {@code Type}.
     *
     * @return {@code 'a'} for {@code Type.ADD}, {@code 'd'} for {@code Type.REMOVE}.
     */
    public byte getId() {
      return id;
    }

    /**
     * Returns the {@code Type} identified by the given {@code byte}.
     *
     * @param id {@code 'a'} for {@code Type.ADD}, {@code 'd'} for {@code Type.REMOVE}.
     * @return The type, if any
     * @throws NoSuchElementException if there is no {@code Type} with the given ID
     */
    public static Type getById(byte id) {
      return switch (id) {
        case 'a' -> ADD;
        case 'd' -> REMOVE;
        default -> throw new NoSuchElementException("No Type with id " + id);
      };
    }

  }

}
