package edu.kit.satviz.serial;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;

public class ClauseUpdateSerialBuilder extends SerialBuilder<ClauseUpdate> {

  // small optimisation because values() has horrible performance characteristics
  private static final ClauseUpdate.Type[] TYPES = ClauseUpdate.Type.values();

  private final SerialBuilder<Clause> clauseSerialBuilder = new ClauseSerialBuilder();

  private ClauseUpdate.Type type;

  @Override
  protected void processAddByte(byte b) throws SerializationException {
    if (type == null) {
      type = TYPES[b];
    } else {
      clauseSerialBuilder.processAddByte(b);
    }
  }

  @Override
  protected ClauseUpdate processGetObject() {
    return new ClauseUpdate(clauseSerialBuilder.getObject(), type);
  }

  @Override
  protected void processReset() {
    type = null;
    clauseSerialBuilder.reset();
  }
}
