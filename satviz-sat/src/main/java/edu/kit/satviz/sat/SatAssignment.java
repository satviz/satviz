package edu.kit.satviz.sat;

public class SatAssignment {

  private final int varCount;
  private final byte[] satAssignment;

  public SatAssignment(int varCount) {
    assert varCount > 0;
    this.varCount = varCount;
    this.satAssignment = new byte[(varCount / 4) + ((varCount % 4 == 0) ? 0 : 1)];
  }

  /**
   * byte = 8 bits
   * => 4 different variables
   */
  public void setVal(int variable, byte value) {
    assert value < 4 && variable < this.varCount;
    int shift = 2 * (variable % 4);
    byte mask = (byte) ~(3 << shift);

    byte result = (byte) (
            (value << shift) | (this.satAssignment[(variable / 4)] & mask)
    );
    this.satAssignment[(variable / 4)] = result;
  }

  public int getVal(int variable) {
    assert variable < this.varCount;
    int shift = 2 * (variable % 4);

    return (this.satAssignment[variable / 4] >> shift) & 3;
  }

  public int getVarCount() {
    return this.varCount;
  }

}
