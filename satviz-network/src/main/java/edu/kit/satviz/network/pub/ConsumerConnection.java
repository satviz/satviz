package edu.kit.satviz.network.pub;

import edu.kit.satviz.network.general.ConnectionServer;
import edu.kit.satviz.network.general.NetworkMessage;
import edu.kit.satviz.network.general.PollEvent;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ConsumerConnection {
  private enum State {
    INIT,
    STARTED,
    CLOSED
  }

  private class ConnectionData {
    public ProducerId pid = null;
    public ConsumerConnectionListener ls = null;
  }

  private final int port;
  private final Consumer<ProducerId> lsConnect;
  private final Consumer<String> lsFail;
  private final List<ConnectionData> connections = new CopyOnWriteArrayList<>();

  private ConnectionServer server = null;
  private final Object SYNC_STATE = new Object();
  private volatile State state = State.INIT;

  public ConsumerConnection(int port, Consumer<ProducerId> lsConnect, Consumer<String> lsFail) {
    this.port = port;
    this.lsConnect = Objects.requireNonNull(lsConnect);
    this.lsFail = Objects.requireNonNull(lsFail);
  }

  public void doClose() {
    // close all single connections
    for (ConnectionData conn : connections) {
      // TODO sync
      if (conn.ls != null) {
        try {
          server.write(conn.pid.getId(), MessageTypes.STOP, null);
        } catch (Exception e) {
          // nothing more to do
        }
      }
    }

    // TODO make sure that no new are added
    if (server != null) {
      server.close();
    }
  }

  private void read (int id, NetworkMessage msg) {
    ConnectionData conn = connections.get(id);
    switch (msg.getType()) {
      case MessageTypes.OFFER -> {
        Map<String, String> offerData = (Map<String, String>) msg.getObject();
        if (offerData.get("type").equals("solver")) {
          connections.get(id).pid = new SolverId(
              id, server.getRemoteAddress(id),
              offerData.get("name"), offerData.get("delayed"), offerData.get("hash")
          );
        } else {
          connections.get(id).pid = new ProofId(
              id, server.getRemoteAddress(id)
          );
        }
        // note: because this thread is busy executing the listener, no messages will be lost
        // while the listener runs.
        // This also means that the listeners should be as fast as possible to avoid becoming a
        // bottleneck
        lsConnect.accept(connections.get(id).pid);
      }
      case MessageTypes.CLAUSE_ADD -> {
        synchronized (conn) {
          if (conn.ls == null) {
            break;
          }
          conn.ls.onClauseUpdate(conn.pid, new ClauseUpdate(
              (Clause) msg.getObject(), ClauseUpdate.Type.ADD
          ));
        }
      }
      case MessageTypes.CLAUSE_DEL -> {
        synchronized (conn) {
          if (conn.ls == null) {
            break;
          }
          conn.ls.onClauseUpdate(conn.pid, new ClauseUpdate(
              (Clause) msg.getObject(), ClauseUpdate.Type.REMOVE
          ));
        }
      }
      case MessageTypes.TERM_SOLVE -> {
        synchronized (conn) {
          if (conn.ls == null) {
            break;
          }
          conn.ls.onTerminateSolved(conn.pid, (SatAssignment) msg.getObject());
          conn.ls = null;
        }
      }
      case MessageTypes.TERM_REFUTE -> {
        synchronized (conn) {
          if (conn.ls == null) {
            break;
          }
          conn.ls.onTerminateRefuted(conn.pid);
          conn.ls = null;
        }
      }
      case MessageTypes.TERM_OTHER -> {
        synchronized (conn) {
          if (conn.ls == null) {
            break;
          }
          conn.ls.onTerminateOtherwise(conn.pid, (String) msg.getObject());
          conn.ls = null;
        }
      }
    }
  }

  private void threadMain() {
    PollEvent event;
    while (true) {
      event = server.poll();
      if (event == null) {
        continue;
      }

      if (event.id() == -1) {
        // TODO global error handling
        // TODO handle errors that appeared because of sync issues (ignore)
      }

      switch (event.type()) {
        case ACCEPT -> connections.add(new ConnectionData());
        case READ -> {
          NetworkMessage msg = (NetworkMessage) event.obj();
          read(event.id(), msg);
        }
        case FAIL -> {
          ConnectionData conn = connections.get(event.id());
          synchronized (conn) {
            if (conn.ls == null) {
              break;
            }
            conn.ls.onTerminateOtherwise(conn.pid, ((Exception) event.obj()).getMessage());
            conn.ls = null;
          }
        }
      }
    }
  }

  public void start() throws IOException {
    synchronized (SYNC_STATE) {
      if (state != State.INIT) {
        throw new IllegalStateException("start already called or connection closed");
      }
      try {
        server = new ConnectionServer(port, MessageTypes.satvizBlueprint);
      } catch (Exception e) {
        state = State.CLOSED;
        throw e;
      }
      state = State.STARTED;
      new Thread(this::threadMain).start();
    }
  }

  public void stop() throws IOException {

  }

  public boolean connect(ProducerId pid, ConsumerConnectionListener ls) {
    ConnectionData conn = connections.get(pid.getId());
    synchronized (conn) {
      conn.ls = ls;
      try {
        server.write(pid.getId(), MessageTypes.START, null);
      } catch (Exception e) {
        conn.ls = null;
        return false;
      }
    }
    return true;
  }

  public boolean disconnect(ProducerId pid) {
    ConnectionData conn = connections.get(pid.getId());
    synchronized (conn) {
      conn.ls = null;
      try {
        server.write(pid.getId(), MessageTypes.STOP, null);
      } catch (Exception e) {
        // listener stays removed in any case
        return false;
      }
    }
    return true;
  }

  public int getPort() throws IOException {
    return server.getLocalAddress().getPort();
  }
}
