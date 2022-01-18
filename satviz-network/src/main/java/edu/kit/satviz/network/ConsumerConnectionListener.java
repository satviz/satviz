package edu.kit.satviz.network;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;

public interface ConsumerConnectionListener {

  void onClauseUpdate(ProducerId pid, ClauseUpdate c);

  void onTerminateSolved(ProducerId pid, SatAssignment sol);

  void onTerminateRefuted(ProducerId pid);

  void onTerminateFailed(ProducerId pid, String reason);
}
