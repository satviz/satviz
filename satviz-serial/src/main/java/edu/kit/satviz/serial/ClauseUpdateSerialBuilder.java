package edu.kit.satviz.serial;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.NoSuchElementException;

/**
 * The {@link SerialBuilder} corresponding to {@link ClauseUpdateSerializer}.
 */
public class ClauseUpdateSerialBuilder extends SerialBuilder<ClauseUpdate> {

  private final SerialBuilder<Clause> clauseSerialBuilder = new ClauseSerialBuilder();

  private ClauseUpdate.Type type;

  @Override
  protected void processAddByte(byte b) throws SerializationException {
    if (type == null) {
      try {
        type = ClauseUpdate.Type.getById(b);
      } catch (NoSuchElementException e) {
        fail("Unknown clause update type " + b);
      }
    } else {
      if (clauseSerialBuilder.addByte(b)) {
        finish();
      }
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
