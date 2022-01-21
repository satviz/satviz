package edu.kit.satviz.serial;

import edu.kit.satviz.sat.SatAssignment;

public class SatAssignmentSerialBuilder extends SerialBuilder<SatAssignment> {
  private final IntSerialBuilder intSerialBuilder = new IntSerialBuilder();
  private boolean isIntSerialBuilderFinished = false;

  private SatAssignment satAssignment;
  private int currentVariable = 1;

  @Override
  public boolean addByte(byte b) throws SerializationException {
    if (this.isIntSerialBuilderFinished) {
      if (currentVariable > satAssignment.getVarCount()) {
        throw new SerializationException("done");
      }
      for (int i = 0; currentVariable <= satAssignment.getVarCount() && i < 4; i++) {
        int offset = (currentVariable & 3) << 1;
        byte value = (byte) ((b >> offset) & 3);
        satAssignment.set(currentVariable++, convertValueToVariableState(value));
      }
      return objectFinished();
    } else {
      if (this.intSerialBuilder.addByte(b)) {
        this.isIntSerialBuilderFinished = true;
        satAssignment = new SatAssignment(this.intSerialBuilder.getObject());
      }
    }
    return false;
  }

  @Override
  public boolean objectFinished() {
    return satAssignment != null && currentVariable == satAssignment.getVarCount();
  }

  @Override
  public SatAssignment getObject() {
    return (objectFinished()) ? satAssignment : null;
  }

  private static SatAssignment.VariableState convertValueToVariableState(byte val) {
    return switch (val) {
      case 0 -> SatAssignment.VariableState.DONTCARE;
      case 1 -> SatAssignment.VariableState.SET;
      case 2 -> SatAssignment.VariableState.UNSET;
      case 3 -> SatAssignment.VariableState.RESERVED;
      default -> throw new IllegalArgumentException("Impossible error, wtf are you doing?");
    };
  }

}
