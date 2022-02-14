package edu.kit.satviz.network;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A clause consumer connection to several clause producers.
 * Wraps {@link ServerConnectionManager}.
 */
public class ConsumerConnection {

  private final ServerConnectionManager conman;
  private final Map<ConnectionId, ProducerId> idMap = new ConcurrentHashMap<>();
  private final Map<ProducerId, ConsumerConnectionListener> listeners = new ConcurrentHashMap<>();

  private Consumer<ProducerId> lsConnect = null;

  private boolean started = false;

  public ConsumerConnection(int port) {
    this.conman = new ServerConnectionManager(port, MessageTypes.satvizBlueprint);
  }

  /**
   * Starts this connection manager.
   * Make sure to call <code>registerConnect</code> to get notified if new producers arrive.
   * Calling this method more than once has no effect.
   *
   * @return whether this was the first time calling this method or not
   */
  public boolean start() {
    synchronized (conman) {
      if (started) {
        return false;
      }
      conman.registerConnect(this::connectListener);
      conman.start();
      started = true;
      return true;
    }
  }

  /**
   * Stops this manager and all associated connections.
   * This method has to be called after working with this connection is done.
   * Otherwise, some threads may not safely exit.
   *
   * @throws InterruptedException if this thread is interrupted waiting on others
   */
  public void stop() throws InterruptedException {
    conman.finishStop();
  }

  /**
   * Registers a listener to listen on global failures.
   *
   * @param ls the listener
   */
  public void registerGlobalFail(Consumer<String> ls) {
    conman.registerGlobalFail(ls);
  }

  public void registerConnect(Consumer<ProducerId> lsConnect) {
    this.lsConnect = lsConnect;
  }

  /**
   * Attempts to establish this connection, so that the producer may start sending data.
   * If the operation fails, the corresponding connection is closed.
   *
   * @param pid the ID of the producer
   * @param ls the listener to be notified on incoming data
   * @return whether the operation succeeded or not
   */
  public boolean connect(ProducerId pid, ConsumerConnectionListener ls) {
    listeners.put(pid, ls);
    try {
      conman.send(pid.cid(), MessageTypes.START, null);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  /**
   * Attempts to close this connection.
   * If the operation fails, the corresponding connection is closed without the producer
   *     receiving a signal.
   *
   * @param pid the ID of the producer
   * @return whether the operation succeeded or not
   */
  public boolean disconnect(ProducerId pid) {
    listeners.put(pid, null); // remove listener
    try {
      conman.send(pid.cid(), MessageTypes.STOP, null);
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  private void callOnClauseUpdate(ProducerId pid, ClauseUpdate c) {
    ConsumerConnectionListener ls = listeners.get(pid);
    if (ls != null) {
      ls.onClauseUpdate(pid, c);
    }
  }

  private void callOnTerminateOtherwise(ProducerId pid, String reason) {
    ConsumerConnectionListener ls = listeners.get(pid);
    if (ls != null) {
      ls.onTerminateOtherwise(pid, reason);
    }
    listeners.put(pid, null); // make sure this is the last received message
  }

  private void callOnTerminateRefuted(ProducerId pid) {
    ConsumerConnectionListener ls = listeners.get(pid);
    if (ls != null) {
      ls.onTerminateRefuted(pid);
    }
    listeners.put(pid, null);
  }

  private void callOnTerminateSolved(ProducerId pid, SatAssignment assign) {
    ConsumerConnectionListener ls = listeners.get(pid);
    if (ls != null) {
      ls.onTerminateSolved(pid, assign);
    }
    listeners.put(pid, null);
  }



  private void connectListener(ConnectionId cid) {
    conman.register(cid, this::messageListener);
  }

  private ProducerId generatePid(ConnectionId cid, Map<String, String> offerData) {
    // no error handling yet
    String type = offerData.get("type");
    if (type.equals("solver")) {
      String name = offerData.get("name");
      int hash = Integer.parseInt(offerData.get("hash"));
      boolean delayed = offerData.get("delayed").equals("true");
      return new ProducerId(cid, OfferType.SOLVER, name, delayed, hash);
    } else {
      return new ProducerId(cid, OfferType.PROOF, null, false, 0);
    }
  }

  private void messageListener(ConnectionId cid, NetworkMessage msg) {
    ProducerId pid = idMap.get(cid);

    switch (msg.getState()) {
      case PRESENT:
        if (pid != null) {
          switch (msg.getType()) {
            case MessageTypes.CLAUSE_ADD -> callOnClauseUpdate(pid,
                new ClauseUpdate((Clause) msg.getObject(), ClauseUpdate.Type.ADD));
            case MessageTypes.CLAUSE_DEL -> callOnClauseUpdate(pid,
                new ClauseUpdate((Clause) msg.getObject(), ClauseUpdate.Type.REMOVE));
            case MessageTypes.TERM_FAIL -> callOnTerminateOtherwise(pid, (String) msg.getObject());
            case MessageTypes.TERM_REFUTE -> callOnTerminateRefuted(pid);
            case MessageTypes.TERM_SOLVE -> callOnTerminateSolved(pid,
                (SatAssignment) msg.getObject());
            default -> { /* ignore */ }
          }
        } else {
          // receive offer packet
          if (msg.getType() == MessageTypes.OFFER) {
            @SuppressWarnings("unchecked")
            Map<String, String> offerData = (Map<String, String>) msg.getObject();
            pid = generatePid(cid, offerData);
            idMap.put(cid, pid);
            if (lsConnect != null) {
              lsConnect.accept(pid);
            }
          }
        }
        break;
      case TERM:
        callOnTerminateOtherwise(pid, "term");
        break;
      case FAIL:
        callOnTerminateOtherwise(pid, "fail");
        break;
      default:
        // ignore
        break;
    }
  }
}
