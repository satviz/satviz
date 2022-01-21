package edu.kit.satviz.serial;

import edu.kit.satviz.sat.SatAssignment;
import edu.kit.satviz.sat.SatAssignment.VariableState;

import java.io.IOException;
import java.io.OutputStream;

public class SatAssignmentSerializer extends Serializer<SatAssignment> {

  @Override
  public void serialize(SatAssignment satAssignment, OutputStream out) throws IOException, SerializationException {
    if (satAssignment == null) {
      throw new SerializationException("The given instance of SatAssignment is null.");
    }

    IntSerializer intSerializer = new IntSerializer();
    intSerializer.serialize(satAssignment.getVarCount(), out);

    byte[] byteArray = new byte[satAssignment.getVarCount() >> 2];
    for (int variable = 1; variable <= satAssignment.getVarCount(); variable++) {
      byteArray[variable - 1] = convertVariableStateToValue(satAssignment.get(variable));
    }
    out.write(byteArray);
  }

  @Override
  public SerialBuilder<SatAssignment> getBuilder() {
    return null;
  }

  private static byte convertVariableStateToValue(VariableState state) {
    return switch (state) {
      case DONTCARE -> 0;
      case SET -> 1;
      case UNSET -> 2;
      case RESERVED -> 3;
    };
  }

}
