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

  public ClauseSerialBuilder() {
    processReset();
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
      fail("invalid unsigned literal mapping value");
    }
    return lit;
  }

  @Override
  protected void processAddByte(byte b) throws SerializationException {
    if (b == 0) {
      if (acc != 0) {
        fail("literal mapping not terminated correctly");
      }
      // crop array
      int[] cutLiterals = new int[numLiterals];
      System.arraycopy(literals, 0, cutLiterals, 0, numLiterals);
      clause = new Clause(cutLiterals);
      finish();
      return;
    }

    if ((b & 0x80) != 0) {
      // literal not done
      acc |= (b & 0x7f) << currentShift;
      currentShift += 7;
      if (currentShift > 28) {
        fail("unsigned literal mapping too big");
      }
    } else {
      // literal done with this byte; add and reset
      acc |= b << currentShift;
      addLiteral(unsignedMappingToLit(acc));
      acc = 0;
      currentShift = 0;
    }
  }

  @Override
  protected Clause processGetObject() {
    return clause;
  }

  @Override
  protected void processReset() {
    numLiterals = 0;
    literals = new int[DEFAULT_CAP];
    clause = null;
    acc = 0;
    currentShift = 0;
  }
}
