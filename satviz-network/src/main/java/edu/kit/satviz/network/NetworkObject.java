package edu.kit.satviz.network;

/**
 * A message with an object transmitted over network.
 * Each message has a type, which denotes the information this message carries.
 *
 * @author luwae
 */
public class NetworkObject {
  /**
   * The state of this message.
   */
  public enum State {
    /** No object or message present. */
    NONE,
    /** An object or message is present. */
    PRESENT,
    /** Indicates that the corresponding connection has failed. */
    FAIL,
    /** Indicates that the corresponding connection has terminated. */
    TERM
  }

  private static final NetworkObject objNone = new NetworkObject(State.NONE);
  private static final NetworkObject objFail = new NetworkObject(State.FAIL);
  private static final NetworkObject objTerm = new NetworkObject(State.TERM);

  /** The state. */
  private final State state;
  /** The message type, as specified by {@link edu.kit.satviz.network.NetworkBlueprint}. */
  private final byte type;
  /** The object contained in this message. Only present if state is <code>PRESENT</code> */
  private final Object obj;

  /**
   * Creates a new message with a specific type, carrying an object.
   *
   * @param type the message type
   * @param obj the message object
   */
  public NetworkObject(byte type, Object obj) {
    this.state = State.PRESENT;
    this.type = type;
    this.obj = obj;
  }

  /**
   * Creates a new message with a specific state.
   *
   * @param state the message state
   */
  private NetworkObject(State state) {
    this.state = state;
    this.type = 0;
    this.obj = null;
  }

  /**
   * Gets a network object with the <code>NONE</code> state.
   *
   * @return a network object
   */
  public static NetworkObject createNone() {
    return objNone;
  }

  /**
   * Gets a network object with the <code>FAIL</code> state.
   *
   * @return a network object
   */
  public static NetworkObject createFail() {
    return objFail;
  }

  /**
   * Gets a network object with the <code>TERM</code> state.
   *
   * @return a network object
   */
  public static NetworkObject createTerm() {
    return objTerm;
  }

  /**
   * Returns the state of this message.
   *
   * @return the state
   */
  public State getState() {
    return state;
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
