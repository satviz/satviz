package edu.kit.satviz.network.general;

/**
 * A message transmitted over network.
 * Each message has a type, which denotes the information this message carries.
 * Additionally, each message may carry an object.
 */
public record NetworkMessage(byte type, Object object) {
}
