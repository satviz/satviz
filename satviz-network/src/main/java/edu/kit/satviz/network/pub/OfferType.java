package edu.kit.satviz.network.pub;

/**
 * A type of clause producer.
 */
public enum OfferType {
    /** Clause producer runs a solver live. */
    SOLVER,
    /** Clause producer replays finished proof. */
    PROOF
}
