package edu.kit.satviz.network;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import java.util.function.Consumer;

/**
 * A clause producer connection to a clause consumer.
 */
public class ProducerConnection {

  private final ClientConnectionManager conman;
  private ConnectionId cid = null;

  private boolean done = false;

  public ProducerConnection(String address, int port) {
    this.conman = new ClientConnectionManager(address, port, MessageTypes.satvizBlueprint);
  }

  public void establish(ProducerId pid) {
    conman.registerConnect(this::connectListener);
    conman.registerGlobalFail(this::globalFailListener);
    conman.start();

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




  private void connectListener(ConnectionId cid) {
    this.cid = cid;
    conman.register(cid, )
  }

  private void globalFailListener(String reason) {
    // TODO
  }

  private void messageListener(ConnectionId cid, NetworkMessage msg) {
    // ignore cid because there is only one
    switch (msg.getState()) {
      case PRESENT -> {
        switch (msg.getType()) {
          case MessageTypes.START -> {/*TODO*/}
          case MessageTypes.STOP -> {/*TODO*/}
          default -> {/*TODO*/}
        }
      }
      case TERM -> {
        /*TODO*/
      }
      case FAIL -> {
        /*TODO*/
      }
    }
  }
}
