package edu.kit.satviz.serial;

import edu.kit.satviz.sat.ClauseUpdate;

public class ClauseUpdateSerialBuilder extends SerialBuilder<ClauseUpdate> {
  @Override
  protected void processAddByte(byte b) throws SerializationException {

  }

  @Override
  protected ClauseUpdate processGetObject() {
    return null;
  }

  @Override
  protected void processReset() {

  }
}
