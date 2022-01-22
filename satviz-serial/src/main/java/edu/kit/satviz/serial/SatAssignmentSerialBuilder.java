package edu.kit.satviz.serial;

import edu.kit.satviz.sat.SatAssignment;

/**
 * TODO
 *
 * @author quorty
 */
public class SatAssignmentSerialBuilder extends SerialBuilder<SatAssignment> {
  private IntSerialBuilder intBuilder;
  private boolean intBuilderDone;

  private SatAssignment assign;
  private int currentVariable;

  public SatAssignmentSerialBuilder() {
    reset();
  }

  @Override
  public boolean addByte(byte b) throws SerializationException {
    if (intBuilderDone) {
      if (currentVariable > assign.getVarCount()) {
        throw new SerializationException("done");
      }
      for (int i = 0; currentVariable <= assign.getVarCount() && i < 4; i++) {
        int shift = (currentVariable & 3) << 1;
        byte value = (byte) ((b >> shift) & 3);
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

  @Override
  public void reset() {
    intBuilder = new IntSerialBuilder();
    intBuilderDone = false;
    assign = null;
    currentVariable = 1;
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
