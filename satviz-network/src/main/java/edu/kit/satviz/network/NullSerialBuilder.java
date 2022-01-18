package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;

/**
 * A {@link SerialBuilder} for a <code>null</code> object.
 * @see NullSerializer
 *
 * @author luwae
 */
public class NullSerialBuilder extends SerialBuilder<Object> {
  private boolean done = false;

  @Override
  public boolean addByte(int i) throws SerializationException {
    if (done) {
      throw new SerializationException("done");
    }

    done = true;
    if (i == 0) {
      return true;
    } else {
      throw new SerializationException("unexpected byte");
    }
  }

  @Override
  public Object getObject() {
    return null;
  }
}
