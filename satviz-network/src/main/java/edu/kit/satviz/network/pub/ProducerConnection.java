package edu.kit.satviz.network.pub;

import edu.kit.satviz.network.general.Connection;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.SerializationException;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProducerConnection {
  private enum State {
    INIT,
    CONNECTING,
    CONNECTED,
    STOPPED
  }

  private final String address;
  private final int port;
  private Connection client = null;
  private final ProducerId pid;
  private final ProducerConnectionListener ls;

  private final Object SYNC_READ = new Object();
  private final Object SYNC_STATE = new Object();
  private volatile boolean shouldClose = false;

  public ProducerConnection(String address, int port, ProducerId pid, ProducerConnectionListener ls) {
    this.address = address;
    this.port = port;
    this.pid = Objects.requireNonNull(pid);
    this.ls = Objects.requireNonNull(ls);
  }

  private void doEstablish() throws IOException, InterruptedException {
    synchronized (SYNC_STATE) { // TODO no explicit sync here; boolean flag; but take care of close
      do {
        try {
          client = new Connection(address, port, MessageTypes.satvizBlueprint);
        } catch (ConnectException e) {
          // connection refused by remote machine (no-one listening on port)
          // try again later
          client = null;
          // we do not expect to be woken up here, but spurious wake-ups don't matter
          SYNC_STATE.wait(1000);
        }
      } while (client == null && !shouldClose);
    }

    Map<String, String> offerData = new HashMap<>();
    offerData.put("version", "1");
    if (pid.type() == OfferType.SOLVER) {
      SolverId solverId = (SolverId) pid;
      offerData.put("type", "solver");
      offerData.put("name", solverId.solverName());
      offerData.put("hash", Long.toString(solverId.instanceHash()));
      offerData.put("delayed", solverId.solverDelayed() ? "true" : "false");
    } else {
      offerData.put("type", "proof");
    }
    try {
      client.write(MessageTypes.OFFER, offerData);
    } catch (SerializationException e) {
      throw new RuntimeException(e); // TODO
    }
  }

  public void sendClauseUpdate(ClauseUpdate c) throws IOException, SerializationException {
    synchronized (SYNC_READ) {
      synchronized (SYNC_STATE) {
        if (...) {
          if (c.type() == ClauseUpdate.Type.ADD) {
            client.write(MessageTypes.CLAUSE_ADD, c.clause());
          } else {
            client.write(MessageTypes.CLAUSE_DEL, c.clause());
          }
        }
      }
    }
  }

  public void close() {
    synchronized (SYNC_STATE) {
      shouldClose = true;
    }
  }
}
