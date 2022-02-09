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
  private ProducerId pid;

  private volatile boolean startReceived = false;
  private ProducerConnectionListener ls = null;

  public ProducerConnection(String address, int port) {
    this.conman = new ClientConnectionManager(address, port, MessageTypes.satvizBlueprint);
  }

  public boolean establish(ProducerId pid) {
    synchronized (conman) {
      if (this.pid == null) {
        this.pid = pid;
        conman.registerConnect(this::connectListener);
        conman.start();
        return true;
      }
      return false;
    }
  }

  private void sendOrTerminate(byte type, Object obj) throws NotYetConnectedException {
    if (!startReceived) { // offer packet not yet through
      throw new NotYetConnectedException();
    }
    try {
      conman.send(cid, type, obj);
    } catch (NotYetConnectedException e) {
      // throw again
      throw e;
    } catch (IOException e) {
      conman.stop();
    }
  }

  private void sendAndTerminate(byte type, Object obj) throws InterruptedException {
    try {
      conman.send(cid, type, obj);
    } catch (NotYetConnectedException | IOException e) {
      // nothing more we can do
    }
    conman.finishStop();
  }

  public void sendClauseUpdate(ClauseUpdate c) throws NotYetConnectedException {
    sendOrTerminate(
        c.type() == ClauseUpdate.Type.ADD ? MessageTypes.CLAUSE_ADD : MessageTypes.CLAUSE_DEL,
        c.clause()
    );
  }

  public void terminateSolved(SatAssignment assign) throws NotYetConnectedException,
      InterruptedException {
    sendAndTerminate(MessageTypes.TERM_SOLVE, assign);
  }

  public void terminateRefuted() throws NotYetConnectedException, InterruptedException {
    sendAndTerminate(MessageTypes.TERM_REFUTE, null);
  }

  public void terminateFailed(String reason) throws NotYetConnectedException,
      InterruptedException {
    sendAndTerminate(MessageTypes.TERM_FAIL, reason);
  }

  public void register(ProducerConnectionListener ls) {
    this.ls = ls;
  }

  public void registerGlobalFail(Consumer<String> ls) {
    conman.registerGlobalFail(ls);
  }




  private void connectListener(ConnectionId cid) {
    this.cid = cid;
    conman.register(cid, this::messageListener);

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

  private void callOnConnect() {
    if (ls != null) {
      ls.onConnect();
    }
  }

  private void callOnDisconnect(String reason) {
    if (ls != null) {
      ls.onDisconnect(reason);
    }
  }

  private void messageListener(ConnectionId cid, NetworkMessage msg) {
    // ignore cid because there is only one
    switch (msg.getState()) {
      case PRESENT:
        switch (msg.getType()) {
          case MessageTypes.START -> {
            startReceived = true;
            callOnConnect();
          }
          case MessageTypes.STOP -> {
            conman.stop();
            callOnDisconnect("stop");
          }
          default -> { /* ignore */ }
        }
        break;
      case TERM:
        conman.stop();
        callOnDisconnect("term");
        break;
      case FAIL:
        conman.stop();
        callOnDisconnect("fail");
        break;
      default:
        // ignore
        break;
    }
  }
}
