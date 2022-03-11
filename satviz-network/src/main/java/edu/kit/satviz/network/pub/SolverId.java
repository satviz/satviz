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

  public String getSolverName() {
    return solverName;
  }

  public boolean isSolverDelayed() {
    return solverDelayed;
  }

  public long getInstanceHash() {
    return instanceHash;
  }
}
