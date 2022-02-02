package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;

/**
 * A {@link SerialBuilder} for a <code>null</code> object.
 *
 * @author luwae
 */
public class NullSerialBuilder extends SerialBuilder<Object> {

  @Override
  protected void processAddByte(byte b) throws SerializationException {
    if (b != 0) {
      fail("unexpected byte");
    }
    finish();
  }

  @Override
  protected Object processGetObject() {
    return null;
  }

  @Override
  protected void processReset() {
    // nothing to reset here
  }
}
