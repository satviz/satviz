package edu.kit.satviz.network.pub;

import edu.kit.satviz.network.general.ConnectionId;

/**
 * An identifier for a producer connection, with associated data.
 */
public record ProducerId(ConnectionId cid, OfferType type,
                         String solverName, boolean solverDelayed, long instanceHash) {
}
