package edu.kit.satviz.network;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;

import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A clause producer connection to a clause consumer.
 */
public class ProducerConnection {

  private final ClientConnectionManager conman;
  private ConnectionId cid = null;

  private ProducerConnectionListener ls = null;
  private Consumer<String> lsFail = null;

  public ProducerConnection(String address, int port) {
    this.conman = new ClientConnectionManager(address, port, MessageTypes.satvizBlueprint);
  }

  private void close(boolean abnormal, String reason) throws InterruptedException {
    conman.stop();
    if (lsFail != null) {
      lsFail.accept(reason);
    }
  }

  public void establish(ProducerId pid) {
    conman.registerConnect(this::connectListener);
    conman.registerGlobalFail(this::globalFailListener);
    conman.start();

    assert (cid != null);
    Map<String, String> offerData = new HashMap<>();
    offerData.put("version", "1");
    if (pid.type() == OfferType.SOLVER) {
      offerData.put("type", "solver");
      offerData.put("name", pid.solverName());
      offerData.put("hash", Integer.toString(pid.instanceHash()));
      offerData.put("delayed", pid.solverDelayed() ? "true" : "false");
    } else {
      offerData.put("type", "proof");
    }
    sendOrTerminate(MessageTypes.OFFER, offerData);
  }

  private void sendOrTerminate(byte type, Object obj) throws NotYetConnectedException {
    try {
      conman.send(cid, type, obj);
    } catch (NotYetConnectedException e) {
      // throw again
      throw e;
    } catch (IOException e) {
      try {
        close(true, "send fail");
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public void sendClauseUpdate(ClauseUpdate c) throws NotYetConnectedException {
    sendOrTerminate(
        c.type() == ClauseUpdate.Type.ADD ? MessageTypes.CLAUSE_ADD : MessageTypes.CLAUSE_DEL,
        c.clause()
    );
  }

  public void terminateSolved(SatAssignment assign) throws NotYetConnectedException {
    // TODO
  }

  public void terminateRefuted() throws NotYetConnectedException {
    // TODO
  }

  public void terminateFailed(String reason) throws NotYetConnectedException {

  }

  public void register(ProducerConnectionListener ls) {
    this.ls = ls;
  }

  public void registerGlobalFail(Consumer<String> ls) {
    this.lsFail = lsFail;
  }




  private void connectListener(ConnectionId cid) {
    this.cid = cid;
    conman.register(cid, this::messageListener);
  }

  private void globalFailListener(String reason) {
    try {
      close(true, reason);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
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
