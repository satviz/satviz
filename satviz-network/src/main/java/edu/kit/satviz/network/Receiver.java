package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import java.nio.ByteBuffer;
import java.util.function.IntFunction;

/**
 * Transforms a byte stream into a network message stream.
 * Messages are deserialized based on given {@link SerialBuilder}s.
 *
 * @author luwae
 */
public class Receiver {
  private final IntFunction<SerialBuilder<?>> gen;
  private byte type;
  private SerialBuilder<?> builder = null;

  /**
   * Creates a new empty receiver.
   *
   * @param gen a function generating {@link SerialBuilder}s from a type
   */
  public Receiver(IntFunction<SerialBuilder<?>> gen) {
    this.gen = gen;
  }

  /**
   * Receives input bytes from a bytebuffer and constructs messages.
   * The bytes previously read are taken into account, so that a long stream
   *     of bytes can be received by subsequent calls to <code>receive</code>.
   * Reads bytes as long as the buffer is not empty and an object is not finished.
   *
   * @param bb the buffer to read from
   * @return a message if one was received in its entirety, <code>null</code> otherwise
   */
  public NetworkMessage receive(ByteBuffer bb) {
    int nb = bb.remaining();
    if (nb == 0) {
      return null;
    }

    if (builder == null) {
      type = bb.get();
      nb--;
      builder = gen.apply(type); // get new builder according to type
      if (builder == null) { // didn't get one
        return NetworkMessage.createFail(); // TODO error handling
      }
    }

    while (nb > 0) {
      nb--;
      boolean done;
      try {
        done = builder.addByte(bb.get()); // TODO use addBytes
      } catch (SerializationException e) {
        return NetworkMessage.createFail();
      }

      if (done) {
        NetworkMessage msg = new NetworkMessage(type, builder.getObject());
        builder = null; // remove last builder
        return msg;
      }
    }
    return null;
  }
}
