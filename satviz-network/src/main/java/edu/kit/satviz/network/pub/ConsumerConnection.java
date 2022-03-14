package edu.kit.satviz.network.pub;

import edu.kit.satviz.network.general.ConnectionServer;
import edu.kit.satviz.network.general.NetworkMessage;
import edu.kit.satviz.network.general.PollEvent;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

// TODO if there is time, make sure serialization or class cast errors are properly handled
// (they appear if the client and server NetworkBlueprint instances differ, so not in our code)

/**
 * The consumer part of a satviz network connection.
 * Events are communicated via listeners. Each single producer connection may have its own
 * {@link ConsumerConnectionListener}, and can be started or stopped independently.
 * The common use case of the connect listener is to install a {@link ConsumerConnectionListener}
 * for the new connection. Additionally, a fail listener is called when a global error occurs.
 */
public class ConsumerConnection {
  private final Object SYNC_START = new Object();
  private boolean started = false;
  private volatile boolean shouldClose = false;

  private static class ConnectionData {
    public ProducerId pid = null;
    public ConsumerConnectionListener ls = null;
    public boolean isDisconnected = false;
  }

  private final int port;
  private final Consumer<ProducerId> lsConnect;
  private final Consumer<String> lsFail;
  private final List<ConnectionData> connections = new CopyOnWriteArrayList<>();

  private ConnectionServer server = null;

  /**
   * Creates a new connection servicing an arbitrary number of producers.
   * {@code lsConnect} should not be {@code null}.
   * @param port the port on which to listen for producers
   * @param lsConnect the connect listener
   * @param lsFail the fail listener
   */
  public ConsumerConnection(int port, Consumer<ProducerId> lsConnect, Consumer<String> lsFail) {
    this.port = port;
    this.lsConnect = Objects.requireNonNull(lsConnect);
    this.lsFail = Objects.requireNonNullElse(lsFail, (s) -> {});
  }

  /**
   * Closes all producer connections by sending them a stop message, and closes the underlying
   * server sockets.
   * Listeners are called optionally, if this close is abnormal.
   * @param failMessage the fail message, {@code null} if orderly close
   */
  private void doClose(String failMessage) {
    // Note: at the moment this is only called from the worker thread! This has numerous
    // advantages: not having to check that no new connections arrive while we close, no
    // global synchronization, no checking if server != null, ...

    // close all single connections
    for (ConnectionData conn : connections) {
      disconnect(conn, failMessage);
    }

    server.close();

    if (failMessage != null) {
      lsFail.accept(failMessage);
    }
  }

  private void read (int id, NetworkMessage msg) {
    ConnectionData conn = connections.get(id);
    switch (msg.getType()) {
      case MessageTypes.OFFER -> {
        InetSocketAddress remote;
        try {
          remote = server.getRemoteAddress(id);
        } catch (Exception e) {
          lsFail.accept("error accepting " + id);
          break; // we can't do anything with this new connection
        }
        @SuppressWarnings("unchecked")
        Map<String, String> offerData = (Map<String, String>) msg.getObject();
        if (offerData.get("type").equals("solver")) {
          connections.get(id).pid = new SolverId(
              id, remote,
              offerData.get("name"),
              offerData.get("delayed").equals("true"),
              Long.parseLong(offerData.get("hash"))
          );
        } else {
          connections.get(id).pid = new ProofId(
              id, remote
          );
        }
        // Note: because this thread is busy executing the listener, no messages will be lost
        // while the listener runs.
        // This also means that the listeners should be as fast as possible to avoid becoming a
        // bottleneck
        lsConnect.accept(connections.get(id).pid);
      }
      case MessageTypes.CLAUSE_ADD -> {
        synchronized (conn) {
          if (conn.isDisconnected || conn.ls == null) {
            break;
          }
          conn.ls.onClauseUpdate(conn.pid, new ClauseUpdate(
              (Clause) msg.getObject(), ClauseUpdate.Type.ADD
          ));
        }
      }
      case MessageTypes.CLAUSE_DEL -> {
        synchronized (conn) {
          if (conn.isDisconnected || conn.ls == null) {
            break;
          }
          conn.ls.onClauseUpdate(conn.pid, new ClauseUpdate(
              (Clause) msg.getObject(), ClauseUpdate.Type.REMOVE
          ));
        }
      }
      case MessageTypes.TERM_SOLVE -> {
        synchronized (conn) {
          if (conn.isDisconnected) {
            break;
          }
          conn.isDisconnected = true;
          if (conn.ls != null) {
            conn.ls.onTerminateSolved(conn.pid, (SatAssignment) msg.getObject());
          }
        }
      }
      case MessageTypes.TERM_REFUTE -> {
        synchronized (conn) {
          if (conn.isDisconnected) {
            break;
          }
          conn.isDisconnected = true;
          if (conn.ls != null) {
            conn.ls.onTerminateRefuted(conn.pid);
          }
        }
      }
      case MessageTypes.TERM_OTHER -> {
        synchronized (conn) {
          if (conn.isDisconnected) {
            break;
          }
          conn.isDisconnected = true;
          if (conn.ls != null) {
            conn.ls.onTerminateOtherwise(conn.pid, (String) msg.getObject());
          }
        }
      }
    }
  }

  private void threadMain() {
    PollEvent event;
    while (true) {
      if (shouldClose) {
        // TODO performance-wise, don't do this in every loop iteration
        // only if the server has to wait (methods for that are already in place)
        doClose(null);
        return;
      }
      event = server.poll();
      if (event == null) {
        continue;
      }

      if (event.id() == -1) {
        // global fail. try our best to close the connections, even though the sends will probably
        // not go through
        doClose("internal server error");
        return;
      }

      switch (event.type()) {
        case ACCEPT -> {
          // Note: we would need to synchronize this if the close action could be performed on
          // another thread than this one
          connections.add(new ConnectionData());
        }
        case READ -> {
          NetworkMessage msg = (NetworkMessage) event.obj();
          read(event.id(), msg);
        }
        case FAIL -> {
          ConnectionData conn = connections.get(event.id());
          synchronized (conn) {
            if (conn.isDisconnected) {
              break;
            }
            conn.isDisconnected = true;
            if (conn.ls != null) {
              conn.ls.onTerminateOtherwise(conn.pid, ((Exception) event.obj()).getMessage());
            }
          }
        }
      }
    }
  }

  /**
   * Starts this ConsumerConnection by creating a {@link ConnectionServer} and worker thread to
   * read messages.
   * This method has no effect if {@code stop()} was called before.
   * @throws IllegalStateException if {@code start()} has already been called
   * @throws IOException if an I/O error occurs
   */
  public void start() throws IOException {
    // Note: currently, this method cannot be used more than once, even if connection creation
    // fails. Maybe we want to use this differently
    synchronized(SYNC_START) {
      if (started) {
        throw new IllegalStateException("start already called");
      }
      started = true;

      if (shouldClose) {
        // not perfectly synchronized, but doesn't matter. worst case, we create the thread and
        // immediately exit
        // perhaps the user wants to know if this case occurred, but throwing an exception seems
        // pretty harsh
        return;
      }

      server = new ConnectionServer(port, MessageTypes.satvizBlueprint);

      new Thread(this::threadMain).start();
    }
  }

  /**
   * Signals this server that it should disconnect all producers and close all underlying sockets.
   */
  public void stop() {
      shouldClose = true;
  }

  /**
   * Registers a listener for a producer and signals the producer to start sending clauses.
   * If the start message cannot be sent, the listener is not registered.
   * If a listener is already registered, the new one will be ignored.
   * @param pid the ID of the connection
   * @param ls the listener
   * @return whether the message was sent or not
   */
  public boolean connect(ProducerId pid, ConsumerConnectionListener ls) {
    // Note: this method can only be called if the worker thread already runs
    // no need to test if server != null
    ConnectionData conn = connections.get(pid.getId());
    synchronized (conn) {
      if (conn.isDisconnected || conn.ls != null) {
        return false;
      }
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

  private boolean disconnect(ConnectionData conn, String failMessage) {
    synchronized (conn) {
      if (conn.isDisconnected) {
        return false;
      }
      conn.isDisconnected = true;

      if (failMessage != null && conn.ls != null) {
        conn.ls.onTerminateOtherwise(conn.pid, failMessage);
      }

      try {
        // send this message even if no listener is registered
        server.write(conn.pid.getId(), MessageTypes.STOP, null);
      } catch (Exception e) {
        // listener stays removed in any case
        return false;
      }
    }
    return true;
  }

  /**
   * Removes a listener from a producer and signals the producer to stop sending clauses.
   * @param pid the ID of the connection
   * @return whether the message was sent or not
   */
  public boolean disconnect(ProducerId pid) {
    return disconnect(connections.get(pid.getId()), null);
  }

  public int getPort() throws IOException {
    return server.getLocalAddress().getPort();
  }
}
