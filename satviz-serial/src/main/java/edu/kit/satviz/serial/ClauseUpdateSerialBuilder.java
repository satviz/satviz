package edu.kit.satviz.serial;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;

/**
 * The {@link SerialBuilder} corresponding to {@link ClauseUpdateSerializer}.
 */
public class ClauseUpdateSerialBuilder extends SerialBuilder<ClauseUpdate> {

  // small optimisation because values() has horrible performance characteristics
  private static final ClauseUpdate.Type[] TYPES = ClauseUpdate.Type.values();

  private final SerialBuilder<Clause> clauseSerialBuilder = new ClauseSerialBuilder();

  private ClauseUpdate.Type type;

  @Override
  protected void processAddByte(byte b) throws SerializationException {
    if (type == null) {
      if (Byte.compareUnsigned(b, (byte) TYPES.length) >= 0) {
        fail("Unknown ClausUpdate type ordinal " + b);
      }
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
