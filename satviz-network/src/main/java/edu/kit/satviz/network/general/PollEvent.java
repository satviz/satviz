package edu.kit.satviz.network.general;

/**
 * An event on a {@link ConnectionServer}.
 * @param type the event type
 * @param id the id of the connection that this event concerns, -1 if global
 * @param obj the object of the event
 */
public record PollEvent(EventType type, int id, Object obj) {
  /** The type of the event. */
  public enum EventType {
    /** A new connection is accepted. {@code obj} is {@code null}. */
    ACCEPT,
    /** A message was read from a connection. {@code obj} is a {@link NetworkMessage} */
    READ,
    /** A connection (or the entire server) failed. {@code obj} is an {@link Exception} */
    FAIL
  }
}
