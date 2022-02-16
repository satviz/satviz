package edu.kit.satviz.network;

/**
 * An identifier for a producer connection, with associated data.
 */
public record ProducerId(ConnectionId cid, OfferType type,
                         String solverName, boolean solverDelayed, int instanceHash) {
}
