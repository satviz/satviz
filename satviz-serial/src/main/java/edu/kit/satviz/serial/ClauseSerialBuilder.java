package edu.kit.satviz.serial;

import edu.kit.satviz.sat.Clause;

/**
 * A {@link SerialBuilder} for SAT clauses.
 * This class uses the variable-length binary DRAT format.
 *
 * @author luwae
 */
public class ClauseSerialBuilder extends SerialBuilder<Clause> {
  /** Default capacity for the growing literal array. */
  private static final int DEFAULT_CAP = 8;

  int numLiterals;
  int[] literals;
  Clause clause;

  int acc;
  int currentShift;

  boolean failed = false;

  public ClauseSerialBuilder() {
    reset();
  }

  private void addLiteral(int lit) {
    if (numLiterals == literals.length) {
      // reallocate with doubled size
      int[] newLiterals = new int[literals.length * 2];
      System.arraycopy(literals, 0, newLiterals, 0, literals.length);
      literals = newLiterals;
    }
    literals[numLiterals++] = lit;
  }

  private int unsignedMappingToLit(int unsignedMapping) throws SerializationException {
    int lit = (unsignedMapping % 2) == 0 ? unsignedMapping / 2 : -(unsignedMapping - 1) / 2;
    if (lit == 0) {
      failed = true;
      throw new SerializationException("invalid unsigned literal mapping value");
    }
    return lit;
  }

  @Override
  public boolean addByte(byte b) throws SerializationException {
    if (objectFinished() || failed) {
      failed = true;
      // TODO encountered problem with failed state; every builder needs this?
      throw new SerializationException("done");
    }

    if (b == 0) {
      // done; add last literal and properly resize the array
      addLiteral(unsignedMappingToLit(acc));
      int[] cutLiterals = new int[numLiterals];
      System.arraycopy(literals, 0, cutLiterals, 0, numLiterals);
      clause = new Clause(cutLiterals);
      return true;
    }

    if ((b & 0x80) != 0) {
      // literal not done
      acc |= (b & 0x7f) << currentShift;
      currentShift += 7;
    } else {
      // literal done with this byte; add and reset
      acc |= b << currentShift;
      addLiteral(unsignedMappingToLit(acc));
      acc = 0;
      currentShift = 0;
    }
    return false;
  }

  @Override
  public boolean objectFinished() {
    return clause != null && !failed;
  }

  @Override
  public Clause getObject() {
    return objectFinished() ? clause : null;
  }

  @Override
  public void reset() {
    numLiterals = 0;
    literals = new int[DEFAULT_CAP];
    clause = null;
    acc = 0;
    currentShift = 0;
    failed = false;
  }
}
