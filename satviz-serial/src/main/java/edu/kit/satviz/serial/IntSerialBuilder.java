package edu.kit.satviz.serial;

/**
 * A {@link SerialBuilder} for integers.
 * Uses little endian format.
 *
 * @author luwae
 */
public class IntSerialBuilder extends SerialBuilder<Integer> {
  int acc = 0;
  int read = 0;

  @Override
  protected void processAddByte(byte b) {
    acc |= (b & 0xff) << (read++ << 3);
    if (read == 4) {
      finish();
    }
  }

  @Override
  protected Integer processGetObject() {
    return acc;
  }

  @Override
  protected void processReset() {
    acc = 0;
    read = 0;
  }
}
