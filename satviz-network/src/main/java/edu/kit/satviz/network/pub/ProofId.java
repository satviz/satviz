package edu.kit.satviz.network.pub;

import java.net.InetSocketAddress;

public final class ProofId extends ProducerId {
  public ProofId(int id, InetSocketAddress address) {
    super(id, address, OfferType.PROOF);
  }

  public ProofId() {
    super(OfferType.PROOF);
  }
}
