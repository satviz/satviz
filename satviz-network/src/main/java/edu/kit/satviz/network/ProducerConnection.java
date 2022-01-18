package edu.kit.satviz.network;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import java.util.function.Consumer;

public class ProducerConnection {

  public void establish(ProducerId pid) {
    // TODO
  }

  public void sendClauseUpdate(ClauseUpdate c) {
    // TODO
  }

  public void terminateSolved(SatAssignment assign) {
    // TODO
  }

  public void terminateRefuted() {
    // TODO
  }

  public void terminateFailed(String reason) {
    // TODO
  }

  public void register(ProducerConnectionListener ls) {
    // TODO
  }

  public void registerGlobalFail(Consumer<String> ls) {
    // TODO
  }
}
