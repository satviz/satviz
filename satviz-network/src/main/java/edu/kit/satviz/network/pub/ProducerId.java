package edu.kit.satviz.network.pub;

/**
 * An identifier for a producer connection, with associated data.
 */
public record ProducerId(int id, OfferType type,
                         String solverName, boolean solverDelayed, long instanceHash) {
}
