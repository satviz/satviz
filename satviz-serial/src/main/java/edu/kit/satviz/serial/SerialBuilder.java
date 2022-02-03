package edu.kit.satviz.serial;

/**
 * A class to deserialize objects of type <code>T</code>.
 * The deserialization process can be done in several steps, as individual bytes are added.
 *
 * @param <T> the type of deserialized objects
 */
public abstract class SerialBuilder<T> {
  private boolean failed = false;
  private boolean finished = false;

  /**
   * Returns whether the deserialization has failed.
   *
   * @return whether it has failed or not
   */
  public final boolean failed() {
    return failed;
  }

  /**
   * Returns whether the deserialization has finished.
   *
   * @return whether it has finished or not
   */
  public final boolean finished() {
    return finished;
  }

  /**
   * Establishes that the current deserialization has failed and throws an according exception.
   * Call this from any concrete subclass in <code>addByte</code>.
   *
   * @param reason the reason of failure
   * @throws SerializationException corresponding exception, always thrown
   */
  protected final void fail(String reason) throws SerializationException {
    failed = true;
    throw new SerializationException(reason);
  }

  /**
   * Establishes that the current deserialization is finished.
   * Call this from any concrete subclass in <code>addByte</code>.
   */
  protected final void finish() {
    finished = true;
  }

  /**
   * Adds a byte to the deserialization process.
   * Adding a byte after the process has finished will change this builder to the failed state.
   *
   * @param b the byte to add
   * @return whether the process is finished or not
   * @throws SerializationException if the process failed or finished before, or failed now
   */
  public final boolean addByte(byte b) throws SerializationException {
    if (failed || finished) {
      failed = true;
      throw new SerializationException("no more bytes expected");
    }
    try {
      processAddByte(b); // may change fail or finish
    } catch (SerializationException e) {
      failed = true;
      throw e;
    }
    return finished;
  }

  /**
   * Gets the finished object.
   *
   * @return the finished object, null if the process has failed or not yet finished
   */
  public final T getObject() {
    if (failed || !finished) {
      return null;
    }
    return processGetObject();
  }

  /**
   * Resets this builder to its initial state.
   */
  public final void reset() {
    failed = false;
    finished = false;
    processReset();
  }

  /**
   * Adds a byte to the deserialization process.
   * This is the primitive method to the template <code>addByte</code>.
   * The concrete class may assume that this method is only called while
   *     the process has not yet failed or finished.
   *
   * @param b the byte to add
   * @throws SerializationException if the byte is invalid
   */
  protected abstract void processAddByte(byte b) throws SerializationException;

  /**
   * Gets the finished object.
   * This is the primitive method to the template <code>getObject</code>.
   * The concrete class may assume that this method is only called while
   *     the process has not failed, and has finished.
   *
   * @return the finished object
   */
  protected abstract T processGetObject();

  /**
   * Resets this builder to its initial state.
   * This is the primitive method to the template <code>reset</code>.
   * The concrete class doesn't have to care about resetting the failed and finished state.
   */
  protected abstract void processReset();
}
