package edu.kit.satviz.network;

import java.net.InetSocketAddress;

/**
 * An identifier for a producer connection, with associated data.
 */
public record ProducerId(ConnectionId cid, OfferType type,
                         String solverName, boolean solverDelayed, int instanceHash) {
}
