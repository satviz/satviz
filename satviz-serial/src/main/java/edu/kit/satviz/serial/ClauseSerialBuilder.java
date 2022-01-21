package edu.kit.satviz.serial;

import edu.kit.satviz.sat.Clause;

public class ClauseSerialBuilder extends SerialBuilder<Clause> {
  private static final int DEFAULT_CAP = 8;

  int numLiterals = 0;
  int[] literals = new int[DEFAULT_CAP];
  Clause clause;

  int currentULit = 0;
  int currentShift = 0;

  private void addLiteral(int lit) {
    if (numLiterals == literals.length) {
      // reallocate
      int[] newLiterals = new int[literals.length * 2];
      System.arraycopy(literals, 0, newLiterals, 0, literals.length);
      literals = newLiterals;
    }
    literals[numLiterals++] = lit;
  }

  @Override
  public boolean addByte(byte b) throws SerializationException {
    if (objectFinished()) {
      throw new SerializationException("done");
    }

    if (b == 0) {
      // done
      addLiteral(currentULit);
      int[] cutLiterals = new int[numLiterals];
      System.arraycopy(literals, 0, cutLiterals, 0, numLiterals);
      clause = new Clause(cutLiterals);
      return true;
    }

    if ((b & 0x80) != 0) {
      // literal not done
      currentULit |= (b & 0x7f) << currentShift;
      currentShift += 7;
    } else {
      // literal done
      currentULit |= b << currentShift;
      int currentLit = ((currentULit % 2) != 0) ? -(currentULit - 1) / 2 : currentULit / 2;
      if (currentLit == 0) {
        throw new SerializationException("invalid unsigned literal mapping value");
      }
      addLiteral(currentLit);
      currentULit = 0;
      currentShift = 0;
    }
    return false;
  }

  @Override
  public boolean objectFinished() {
    return clause != null;
  }

  @Override
  public Clause getObject() {
    return clause;
  }
}
