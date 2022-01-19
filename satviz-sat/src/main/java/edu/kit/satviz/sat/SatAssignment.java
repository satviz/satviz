package edu.kit.satviz.sat;

public class SatAssignment {
  public static enum VariableState {
    DONTCARE(0),
    SET(1),
    UNSET(2),
    RESERVED(3);

    public final byte val;

    VariableState(int val) {
      this.val = (byte) val;
    }

    public static VariableState fromValue(int val) {
      switch(val) {
        case 0: return DONTCARE;
        case 1: return SET;
        case 2: return UNSET;
        case 3: return RESERVED;
      }
      return null;
    }
  }

  private final int varCount;
  private final byte[] satAssignment;

  public SatAssignment(int varCount) {
    assert varCount > 0;
    this.varCount = varCount;
    this.satAssignment = new byte[(varCount + 4) >> 2];
  }

  /**
   * byte = 8 bits
   * => 4 different variables
   */
  public void set(int variable, VariableState state) {
    if (variable <= 0 || variable > varCount) {
      return;
    }

    int shift = (variable & 3) << 1;
    byte mask = (byte) ~(3 << shift);

    byte result = (byte) (
            (state.val << shift) | (this.satAssignment[(variable >> 2)] & mask)
    );
    this.satAssignment[(variable >> 2)] = result;
  }

  public VariableState get(int variable) {
    if (variable <= 0 || variable > varCount) {
      return VariableState.DONTCARE;
    }

    int shift = (variable & 3) << 1;

    return VariableState.fromValue((this.satAssignment[variable >> 2] >> shift) & 3);
  }

  public int getIntState(int variable) {
    VariableState state = get(variable);
    return switch (state) {
      case SET -> variable;
      case UNSET -> -variable;
      default -> 0;
    };
  }

  public int getVarCount() {
    return this.varCount;
  }

}
