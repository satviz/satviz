package edu.kit.satviz.network.pub;

import java.net.InetSocketAddress;

public final class SolverId extends ProducerId {
  private final String solverName;
  private final boolean solverDelayed;
  private final long instanceHash;

  public SolverId(int id, InetSocketAddress address, String solverName, boolean solverDelayed,
      long instanceHash) {
    super(id, address, OfferType.SOLVER);
    this.solverName = solverName;
    this.solverDelayed = solverDelayed;
    this.instanceHash = instanceHash;
  }

  public SolverId(String solverName, boolean solverDelayed, long instanceHash) {
    super(OfferType.SOLVER);
    this.solverName = solverName;
    this.solverDelayed = solverDelayed;
    this.instanceHash = instanceHash;
  }

  public String solverName() {
    return solverName();
  }

  public boolean solverDelayed() {
    return solverDelayed;
  }

  public long instanceHash() {
    return instanceHash();
  }
}
