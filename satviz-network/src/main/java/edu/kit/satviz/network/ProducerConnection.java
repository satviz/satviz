package edu.kit.satviz.network;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A clause producer connection to a clause consumer.
 * Wraps {@link ClientConnectionManager}.
 */
public class ProducerConnection {

  private final ClientConnectionManager conman;
  private ConnectionId cid = null;
  private ProducerId pid = null;

  private volatile boolean startReceived = false;
  private volatile boolean stopReceived = false;
  private ProducerConnectionListener ls = null;

  public ProducerConnection(String address, int port) {
    this.conman = new ClientConnectionManager(address, port, MessageTypes.satvizBlueprint);
  }

  /**
   * Starts establishing the connection to the consumer.
   * Make sure to register a {@link ProducerConnectionListener} if you want to be notified
   *     as soon as this process is done.
   * Calling this method more than once has no effect.
   *
   * @param pid the ID that is sent to the client. The address is ignored.
   * @return whether this was the first time calling this method or not
   */
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

  private void sendOrTerminate(byte type, Object obj, boolean checkState)
      throws IllegalStateException {
    if (checkState && (!startReceived || stopReceived)) {
      throw new IllegalStateException("server not expecting messages");
    }
    try {
      conman.send(cid, type, obj);
    } catch (IOException e) {
      conman.stop(); // globally terminate
    }
  }

  private void sendAndTerminate(byte type, Object obj) {
    try {
      conman.send(cid, type, obj);
    } catch (IllegalStateException | IOException e) {
      // nothing more we can do
    }
    conman.stop(); // globally terminate
  }

  /**
   * Sends a clause update to the consumer.
   *
   * @param c the update
   * @throws IllegalStateException if the consumer is not expecting data
   */
  public void sendClauseUpdate(ClauseUpdate c) throws IllegalStateException {
    sendOrTerminate(
        c.type() == ClauseUpdate.Type.ADD ? MessageTypes.CLAUSE_ADD : MessageTypes.CLAUSE_DEL,
        c.clause(),
        true
    );
  }

  /**
   * Terminates this connection with a satisfying SAT assignment.
   *
   * @param assign the assignment
   */
  public void terminateSolved(SatAssignment assign) {
    sendAndTerminate(MessageTypes.TERM_SOLVE, assign);
  }

  /**
   * Terminates this connection with a refutation message.
   */
  public void terminateRefuted() {
    sendAndTerminate(MessageTypes.TERM_REFUTE, null);
  }

  /**
   * Terminates this connection with a fail message.
   *
   * @param reason the message of failure
   */
  public void terminateFailed(String reason) {
    sendAndTerminate(MessageTypes.TERM_FAIL, reason);
  }

  /**
   * Stops this connection.
   * This method has to be called after working with this connection is done.
   * Otherwise, some threads may not safely exit.
   *
   * @throws InterruptedException if this thread is interrupted waiting on others
   */
  public void stop() throws InterruptedException {
    conman.finishStop();
  }

  /**
   * Registers a listener to listen on this connection.
   *
   * @param ls the listener
   */
  public void register(ProducerConnectionListener ls) {
    this.ls = ls;
  }

  /**
   * Registers a listener to listen on global failures.
   *
   * @param ls the listener
   */
  public void registerGlobalFail(Consumer<String> ls) {
    conman.registerGlobalFail(ls);
  }




  private void connectListener(ConnectionId cid) {
    // called once the connection to the server is established
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
    sendOrTerminate(MessageTypes.OFFER, offerData, false);
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
            stopReceived = true;
            conman.stop();
            callOnDisconnect("stop");
          }
          default -> { /* ignore */ }
        }
        break;
      case TERM:
        stopReceived = true;
        conman.stop();
        callOnDisconnect("term");
        break;
      case FAIL:
        stopReceived = true;
        conman.stop();
        callOnDisconnect("fail");
        break;
      default:
        // ignore
        break;
    }
  }
}
