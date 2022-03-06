package edu.kit.satviz.network.general;

/**
 * A message transmitted over network.
 * Each message has a type, which denotes the information this message carries.
 * Additionally, each message can carry an object.
 */
public class NetworkMessage {

  private final byte type;
  /** The object contained in this message. Only relevant if state is <code>PRESENT</code> */
  private final Object obj;

  /**
   * Creates a new message with a specific type, carrying an object.
   *
   * @param type the message type
   * @param obj the message object
   */
  public NetworkMessage(byte type, Object obj) {
    this.type = type;
    this.obj = obj;
  }

  /**
   * Returns the type of this message.
   * The type is only of relevance if the state is <code>PRESENT</code>.
   *
   * @return the type if present, <code>0</code> otherwise
   */
  public byte getType() {
    return type;
  }

  /**
   * Returns the object associated with this message.
   * The object is only of relevance if the state is <code>PRESENT</code>.
   *
   * @return the object if present, <code>null</code> otherwise
   */
  public Object getObject() {
    return obj;
  }
}
