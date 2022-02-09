package edu.kit.satviz.network;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static edu.kit.satviz.network.MessageTypes.OFFER;

public class ConsumerConnectionManager {

  private final ConnectionManager conman;
  private Map<ConnectionId, ProducerId> idMap = new HashMap<>();
  private Map<ProducerId, ConsumerConnectionListener> listeners = new HashMap<>();

  private boolean started = false;

  public ConsumerConnectionManager(int port) {
    this.conman = new ConnectionManager(port, MessageTypes.satvizBlueprint);
  }

  public boolean start() {
    synchronized (conman) {
      if (started) {
        return false;
      }
      conman.registerConnect(this::connectListener);
      conman.start();
      return true;
    }
  }

  public void stop() throws InterruptedException {
    conman.finishStop();
  }

  public List<ProducerId> getProducers() {
    return idMap.values().stream().toList();
  }

  public void registerGlobalFail(Consumer<String> ls) {
    // TODO
  }

  public void connect(ProducerId pid, ConsumerConnectionListener ls) {
    // TODO
  }

  public void disconnect(ProducerId pid, ConsumerConnectionListener ls) {
    // TODO
  }

  private void callOnClauseUpdate(ConnectionId cid, ClauseUpdate c) {
    ProducerId pid = idMap.get(cid);
    if (pid == null) {
      return;
    }
    ConsumerConnectionListener ls = listeners.get(pid);
    if (ls != null) {
      ls.onClauseUpdate(pid, c);
    }
  }

  private void callOnTerminateOtherwise(ConnectionId cid, String reason) {
    ProducerId pid = idMap.get(cid);
    if (pid == null) {
      return;
    }
    ConsumerConnectionListener ls = listeners.get(pid);
    if (ls != null) {
      ls.onTerminateOtherwise(pid, reason);
    }
  }

  private void callOnClauseAdd(ConnectionId cid, ClauseUpdate c) {
    ProducerId pid = idMap.get(cid);
    if (pid == null) {
      return;
    }
    ConsumerConnectionListener ls = listeners.get(pid);
    if (ls != null) {
      ls.onClauseUpdate(pid, c);
    }
  }


  private void connectListener(ConnectionId cid) {
    conman.register(cid, this::messageListener);
  }

  private void messageListener(ConnectionId cid, NetworkMessage msg) {
    ProducerId pid = idMap.get(cid);
    if (pid == null) {
      // no pid -> no listener connected yet
      if (msg.getState() == NetworkMessage.State.PRESENT && msg.getType() == OFFER) {
        Map<String, String> offerData = (Map<String, String>) msg.getObject();
        String type = offerData.get("type");
        if (type.equals("solver")) {
          String name = offerData.get("name");
          int hash = Integer.parseInt(offerData.get("hash"));
          boolean delayed = offerData.get("delayed").equals("true");
          pid = new ProducerId(cid.address(), OfferType.SOLVER, name, delayed, hash);
        } else {
          pid = new ProducerId(cid.address(), OfferType.PROOF, null, false, 0);
        }
        idMap.put(cid, pid);
      }
    } else {
      switch (msg.getState()) {
        case PRESENT:
          switch (msg.getType()) {
            case MessageTypes.CLAUSE_ADD -> {
              callOnClauseUpdate(cid,
                  new ClauseUpdate((Clause) msg.getObject(), ClauseUpdate.Type.ADD));
            }
            case MessageTypes.CLAUSE_DEL -> {
              callOnClauseUpdate(cid,
                  new ClauseUpdate((Clause) msg.getObject(), ClauseUpdate.Type.REMOVE));
            }
            // TODO terminate
            default -> { /* ignore */ }
          }
          break;
        case TERM:
          callOnTerminateOtherwise(cid, "term");
          break;
        case FAIL:
          callOnTerminateOtherwise(cid, "fail");
          break;
        default:
          // ignore
          break;
      }
    }
  }
}
