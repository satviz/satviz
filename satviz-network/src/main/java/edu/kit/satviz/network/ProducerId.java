package edu.kit.satviz.network;

import java.net.InetSocketAddress;

public class ProducerId {

  public ProducerId(InetSocketAddress addr, OfferType type, String solverName,
      boolean solverDelayed, int instanceHash) {
    // TODO
  }

  public InetSocketAddress getAddress() {
    return null;
  }

  public OfferType getOfferType() {
    return null;
  }

  public String getSolverName() {
    return null;
  }

  public boolean getSolverDelayed() {
    return false;
  }

  public int getInstanceHash() {
    return 0;
  }
}
