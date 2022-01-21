package edu.kit.satviz.serial;

import edu.kit.satviz.sat.SatAssignment;

public class SatAssignmentSerialBuilder extends SerialBuilder<SatAssignment> {
  private final IntSerialBuilder intBuilder = new IntSerialBuilder();
  private boolean intBuilderDone = false;

  private SatAssignment assign;
  private int currentVariable = 1;

  @Override
  public boolean addByte(byte b) throws SerializationException {
    if (intBuilderDone) {
      if (currentVariable > assign.getVarCount()) {
        throw new SerializationException("done");
      }
      for (int i = 0; currentVariable <= assign.getVarCount() && i < 4; i++) {
        int offset = (currentVariable & 3) << 1;
        byte value = (byte) ((b >> offset) & 3);
        assign.set(currentVariable++, convertValueToVariableState(value));
      }
      return objectFinished();
    } else {
      if (intBuilder.addByte(b)) {
        intBuilderDone = true;
        assign = new SatAssignment(intBuilder.getObject());
      }
    }
    return false;
  }

  @Override
  public boolean objectFinished() {
    return assign != null && currentVariable == assign.getVarCount();
  }

  @Override
  public SatAssignment getObject() {
    return (objectFinished()) ? assign : null;
  }

  private static SatAssignment.VariableState convertValueToVariableState(byte val) {
    return switch (val) {
      case 0 -> SatAssignment.VariableState.DONTCARE;
      case 1 -> SatAssignment.VariableState.SET;
      case 2 -> SatAssignment.VariableState.UNSET;
      case 3 -> SatAssignment.VariableState.RESERVED;
      default -> throw new IllegalArgumentException("Impossible error, what are you doing?");
    };
  }

}
