package edu.kit.satviz.network.pub;

import java.net.InetSocketAddress;

/**
 * An identifier for a producer learning clauses from a solver.
 */
public final class SolverId extends ProducerId {
  private final String solverName;
  private final boolean solverDelayed;
  private final long instanceHash;

  /**
   * Creates a new Solver ID, holding all associated data.
   * @param id the ID number of the producer
   * @param address the remote address of the producer
   * @param solverName the name of the solver
   * @param solverDelayed whether the solver waits for a response
   * @param instanceHash the hash of the SAT instance
   */
  public SolverId(int id, InetSocketAddress address, String solverName, boolean solverDelayed,
      long instanceHash) {
    super(id, address, OfferType.SOLVER);
    this.solverName = solverName;
    this.solverDelayed = solverDelayed;
    this.instanceHash = instanceHash;
  }

  /**
   * Creates a bew Solver ID that does not hold ID number and remote address.
   * @param solverName the name of the solver
   * @param solverDelayed whether the solver waits for a response
   * @param instanceHash the hash of the SAT instance
   */
  public SolverId(String solverName, boolean solverDelayed, long instanceHash) {
    super(OfferType.SOLVER);
    this.solverName = solverName;
    this.solverDelayed = solverDelayed;
    this.instanceHash = instanceHash;
  }

  /**
   * Returns the name of the solver that this producer uses.
   * @return solver name
   */
  public String getSolverName() {
    return solverName;
  }

  /**
   * Returns whether the solver is delayed.
   * @return solver delay
   */
  public boolean isSolverDelayed() {
    return solverDelayed;
  }

  /**
   * Returns the hash of the SAT instance the producer uses.
   * @return instance hash
   */
  public long getInstanceHash() {
    return instanceHash;
  }
}
