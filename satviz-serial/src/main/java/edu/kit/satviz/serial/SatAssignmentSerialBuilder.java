package edu.kit.satviz.serial;

import edu.kit.satviz.sat.SatAssignment;

/**
 * A {@link SerialBuilder} for SAT assignments.
 *
 * @author quorty
 */
public class SatAssignmentSerialBuilder extends SerialBuilder<SatAssignment> {
  private final IntSerialBuilder intBuilder = new IntSerialBuilder();
  private boolean intBuilderDone = false;

  private SatAssignment assign = null;
  private int currentVariable = 1;

  @Override
  protected void processAddByte(byte b) throws SerializationException {
    if (intBuilderDone) {
      for (int i = 0; currentVariable <= assign.getVarCount() && i < 4; i++) {
        int shift = (currentVariable & 3) << 1;
        byte value = (byte) ((b >> shift) & 3);
        assign.set(currentVariable++, convertValueToVariableState(value));
      }
      if (currentVariable > assign.getVarCount()) {
        finish();
      }
    } else {
      if (intBuilder.addByte(b)) {
        intBuilderDone = true;
        Integer i = intBuilder.getObject();
        if (i == null) {
          fail("unexpected null integer");
          return; // never reached, but removes NullPointerException warning
        }
        assign = new SatAssignment(i);
      }
    }
  }

  @Override
  protected SatAssignment processGetObject() {
    return assign;
  }

  @Override
  protected void processReset() {
    intBuilder.reset();
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
      default -> null;
    };
  }

}
