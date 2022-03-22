package edu.kit.satviz.network.pub;

import java.net.InetSocketAddress;

/**
 * An identifier for a producer reading clauses from a proof file.
 */
public final class ProofId extends ProducerId {
  /**
   * Creates a new proof ID, holding an ID number and the remote address.
   * @param id the ID number of the producer
   * @param address the remote address of the producer
   */
  public ProofId(int id, InetSocketAddress address) {
    super(id, address, OfferType.PROOF);
  }

  /**
   * Creates a new proof ID that does not hold ID number and remote address.
   */
  public ProofId() {
    super(OfferType.PROOF);
  }
}
