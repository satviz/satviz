package edu.kit.satviz.network;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
  private boolean onConnectSent = false;
  private boolean onDisconnectSent = false;

  /**
   * Creates a new producer connection that connects to the specified address and port.
   *
   * @param address the remote address
   * @param port the remote port
   */
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
   */
  public void establish(ProducerId pid) {
    synchronized (this) {
      if (this.pid == null) {
        this.pid = Objects.requireNonNull(pid);
        conman.registerConnect(this::connectListener);
        conman.start();
      }
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
      close("send error");
    }
  }

  private void sendAndTerminate(byte type, Object obj) {
    try {
      conman.send(cid, type, obj);
    } catch (IllegalStateException | IOException e) {
      // nothing more we can do
    }
    close("term");
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
   * Waits for the network thread to exit (optional).
   * Only call this if you have called either <code>terminateSolved</code>,
   *     <code>terminateRefuted</code>, or <code>terminateFailed</code>.
   *
   * @throws InterruptedException if this thread is interrupted waiting on others
   */
  public void stop() throws InterruptedException {
    conman.finishStop();
  }

  private void close(String reason) {
    conman.stop();
    callOnDisconnect(reason);
  }

  /**
   * Registers a listener to listen on this connection.
   * The listener cannot be changed after it has been assigned.
   *
   * @param ls the listener
   */
  public void register(ProducerConnectionListener ls) {
    synchronized (this) {
      if (this.ls == null) {
        this.ls = ls;
      }
    }
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
      offerData.put("hash", Long.toString(pid.instanceHash()));
      offerData.put("delayed", pid.solverDelayed() ? "true" : "false");
    } else {
      offerData.put("type", "proof");
    }
    sendOrTerminate(MessageTypes.OFFER, offerData, false);
  }

  private void callOnConnect() {
    synchronized (this) {
      if (onConnectSent || ls == null) {
        return;
      }
      onConnectSent = true;
      ls.onConnect();
    }
  }

  private void callOnDisconnect(String reason) {
    synchronized (this) {
      if (onDisconnectSent || ls == null) {
        return;
      }
      onDisconnectSent = true;
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
            close("stop");
          }
          default -> { /* ignore */ }
        }
        break;
      case TERM:
        stopReceived = true;
        close("term");
        break;
      case FAIL:
        stopReceived = true;
        close("fail");
        break;
      default:
        // ignore
        break;
    }
  }
}
