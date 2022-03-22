package edu.kit.satviz.network.pub;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;

/**
 * Callback methods for the consumer side listening on a producer connection.
 */
public interface ConsumerConnectionListener {

  /**
   * Called when a clause was sent.
   *
   * @param pid ID of sending producer
   * @param c the clause update
   */
  default void onClauseUpdate(ProducerId pid, ClauseUpdate c) {
    // do nothing
  }

  /**
   * Called when a satisfying SAT assignment was sent.
   *
   * @param pid ID of sending producer
   * @param assign the satisfying assignment
   */
  default void onTerminateSolved(ProducerId pid, SatAssignment assign) {
    // do nothing
  }

  /**
   * Called when the solver/proof is done and no solution was found.
   *
   * @param pid ID of sending producer
   */
  default void onTerminateRefuted(ProducerId pid) {
    // do nothing
  }

  /**
   * Called when the solver/proof failed or stopped some other way.
   *
   * @param pid ID of sending producer
   * @param reason the reason for termination
   */
  default void onTerminateOtherwise(ProducerId pid, String reason) {
    // do nothing
  }
}
