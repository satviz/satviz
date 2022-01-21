package edu.kit.satviz.serial;

/**
 * A {@link SerialBuilder} for integers.
 *
 * @author luwae
 */
public class IntSerialBuilder extends SerialBuilder<Integer> {
  private int acc;
  private int numByte;

  public IntSerialBuilder() {
    reset();
  }

  @Override
  public boolean addByte(byte b) throws SerializationException {
    if (numByte == 4) {
      throw new SerializationException("done");
    }

    acc |= b << (numByte++ << 3);

    return numByte == 4;
  }

  @Override
  public boolean objectFinished() {
    return numByte == 4;
  }

  @Override
  public Integer getObject() {
    return (numByte == 4) ? acc : null;
  }

  @Override
  public void reset() {
    acc = 0;
    numByte = 0;
  }
}
