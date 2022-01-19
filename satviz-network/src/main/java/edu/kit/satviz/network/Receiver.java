package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import java.nio.ByteBuffer;
import java.util.function.IntFunction;

/**
 * Transforms a byte stream into an object stream.
 * Objects are deserialized based on given {@link edu.kit.satviz.serial.SerialBuilder}s.
 *
 * @author luwae
 */
public class Receiver {
  private final IntFunction<SerialBuilder<?>> gen;
  private byte type;
  private SerialBuilder<?> builder = null;
  private NetworkObject netObj = null;

  /**
   * Creates a new empty receiver.
   *
   * @param gen a function generating {@link edu.kit.satviz.serial.SerialBuilder}s from a type
   */
  public Receiver(IntFunction<SerialBuilder<?>> gen) {
    this.gen = gen;
  }

  /**
   * Receives input bytes from a bytebuffer and constructs objects.
   * Reads bytes as long as the buffer is not empty and an object is not finished.
   *
   * @param bb the buffer to read from
   * @return <code>true</code> if an object is present, <code>false</code> otherwise.
   */
  public boolean receive(ByteBuffer bb) {
    int nb = bb.remaining();
    if (nb == 0) {
      return false;
    }

    if (builder == null) {
      type = bb.get();
      builder = gen.apply(type);
      nb--;
    }

    while (nb > 0) {
      nb--;
      boolean done;
      try {
        done = builder.addByte(bb.get());
      } catch (SerializationException e) {
        netObj = NetworkObject.createFail();
        return true;
      }

      if (done) {
        netObj = new NetworkObject(type, builder.getObject());
        return true;
      }
    }
    return false;
  }

  /**
   * Gets a finished network object.
   * Removes the stored object from this class.
   *
   * @return the network object, one with <code>State.NONE</code> if none present
   */
  public NetworkObject getObject() {
    if (netObj != null) {
      NetworkObject tmp = netObj;
      netObj = null;
      return tmp;
    }
    return NetworkObject.createNone();
  }
}
