package edu.kit.satviz.serial;

import edu.kit.satviz.sat.SatAssignment;
import edu.kit.satviz.sat.SatAssignment.VariableState;
import java.io.IOException;
import java.io.OutputStream;

public class SatAssignmentSerializer extends Serializer<SatAssignment> {

  @Override
  public void serialize(SatAssignment assign, OutputStream out) throws IOException, SerializationException {
    IntSerializer intSerializer = new IntSerializer();
    intSerializer.serialize(assign.getVarCount(), out);

    byte b = 0;
    for (int i = 1; i <= assign.getVarCount(); i++) {
      byte val = convertVariableStateToValue(assign.get(i));
      int shift = (i & 3) << 1;
      b |= val << shift;
      if ((i & 3) == 0) {
        out.write(b);
        b = 0;
      }
    }
  }

  @Override
  public SerialBuilder<SatAssignment> getBuilder() {
    return new SatAssignmentSerialBuilder();
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
