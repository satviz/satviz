package edu.kit.satviz.network.pub;

import edu.kit.satviz.network.general.Connection;
import edu.kit.satviz.network.general.NetworkMessage;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

/**
 * The producer part of a satviz network connection.
 * State changes are communicated via the {@link ProducerConnectionListener}. {@code onConnect} is
 *     called once the consumer sends the START signal. {@code onDisconnect} is called if an
 *     internal error occurs or the consumer sends the STOP signal.
 */
public class ProducerConnection {
  private enum State {
    INIT,
    ESTABLISHING,
    ESTABLISHED,
    STARTED,
    CLOSED
  }

  private final String address;
  private final int port;
  private final ProducerId pid;
  private final ProducerConnectionListener ls;

  private Connection client = null;
  private final Object SYNC_STATE = new Object();
  private volatile State state = State.INIT;

  private String termMessage = null;
  private byte termByte = 0;
  private Object termObject = null;

  /**
   * Creates a new connection to a consumer.
   * Does not try to connect to the consumer; this is done in {@code establish()}.
   * @param address the consumer address
   * @param port the consumer port
   * @param pid the type of clause source that this producer represents, not {@code null}
   * @param ls the event listener, not {@code null}
   */
  public ProducerConnection(String address, int port, ProducerId pid, ProducerConnectionListener ls) {
    this.address = address;
    this.port = port;
    this.pid = Objects.requireNonNull(pid);
    this.ls = Objects.requireNonNull(ls);
  }

  private void doCloseFromThread(String termMessage, byte termByte, Object termObject) {
    // TODO check that this is only called once, but exactly once

    synchronized (SYNC_STATE) {
      if (state == State.CLOSED) { // user closed
        termMessage = this.termMessage;
        termByte = this.termByte;
        termObject = this.termObject;
      }
      state = State.CLOSED;

      if (client != null) {
        if (termByte != 0) {
          try {
            client.write(termByte, termObject);
          } catch (Exception e) {
            // nothing
          }
        }
        client.close();
      }

      if (termMessage != null) {
        ls.onDisconnect(termMessage);
      }
    }
  }

  /**
   * Establishes the connection to the consumer.
   * Repeatedly creates {@link Connection}s until one successfully connects to the consumer. Then
   *     the offer message is sent.
   * Closes this producer connection if something goes wrong.
   * @return true if the establishing was successful, false if not
   */
  private boolean doEstablish() {
    Map<String, String> offerData = new HashMap<>();
    offerData.put("version", "1");
    if (pid.getType() == OfferType.SOLVER) {
      SolverId solverId = (SolverId) pid;
      offerData.put("type", "solver");
      offerData.put("name", solverId.getSolverName());
      offerData.put("delayed", solverId.isSolverDelayed() ? "true" : "false");
      offerData.put("hash", Long.toString(solverId.getInstanceHash()));
    } else {
      offerData.put("type", "proof");
    }

    synchronized (SYNC_STATE) {
      while (client == null && state == State.ESTABLISHING) {
        try {
          client = new Connection(address, port, MessageTypes.satvizBlueprint);
        } catch (ConnectException e) {
          // connection refused by remote machine (no-one listening on port)
          // try again later
          client = null;
          try {
            SYNC_STATE.wait(1000);
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            state = State.CLOSED;
            ls.onDisconnect("fail: establish interrupted");
            return false;
          }
        } catch (Exception e) {
          state = State.CLOSED;
          ls.onDisconnect("fail: establish connect");
          return false;
        }
      }

      if (client == null) { // state must have been set to State.CLOSED by the user
        // nothing we need to do here
        return false;
      }

      try {
        client.write(MessageTypes.OFFER, offerData);
      } catch (Exception e) {
        state = State.CLOSED;
        ls.onDisconnect("fail: establish offer");
        return false;
      }

      state = State.ESTABLISHED;
      return true;
    }
  }

  private void threadMain() {
    if (!doEstablish()) {
      // some sort of fatal error occurred
      // state is set to closed and connection is closed
      return;
    }

    // Note: we cannot have the read() call in a synchronized block, as this would interfere
    // with writing. Waiting for up to one second in read() while we cannot write is not desirable.
    // This means we have to be careful here, as we may read past a close(). Sometimes this is
    // unavoidable, and not indicative of a synchronization mistake.

    Selector sel = null;
    try {
      sel = Selector.open();
      client.register(sel, SelectionKey.OP_READ);
    } catch (Exception e) {
      if (sel != null) {
        try {
          sel.close();
        } catch (Exception ex) {
          // nothing
        }
      }
      doCloseFromThread("fail: selector", MessageTypes.TERM_OTHER, "fail: selector");
      return;
    }

    Queue<NetworkMessage> readQueue;
    while (true) {
      synchronized (SYNC_STATE) {
        if (state == State.CLOSED) {
          doCloseFromThread(null, (byte) 0, null);
          return;
        }
      }

      try {
        sel.select(1000); // avoid busy-wait
        sel.selectedKeys().clear(); // act like we took care of everything
        readQueue = client.read();
      } catch (Exception e) {
        doCloseFromThread("fail: read", MessageTypes.TERM_OTHER, "fail: read");
        return;
      }

      for (NetworkMessage msg: readQueue) {
        switch (msg.type()) {
          case MessageTypes.START -> {
            synchronized (SYNC_STATE) {
              if (state == State.CLOSED) {
                doCloseFromThread(null, (byte) 0, null);
                return;
              }
              state = State.STARTED;
              ls.onConnect();
            }
          }
          case MessageTypes.STOP -> {
            doCloseFromThread("stop", (byte) 0, null);
            return;
          }
          default -> { /* ignore */ }
        }
      }
    }
  }

  /**
   * Establishes the connection to the consumer by spawning a worker thread to read messages.
   * The thread terminates if an internal error occurs or one of the terminate methods is called.
   * @throws IllegalStateException if {@code establish()} has already been called or this
   *     ProducerConnection is closed
   */
  public void establish() {
    synchronized (SYNC_STATE) {
      if (state != State.INIT) {
        throw new IllegalStateException("establish already called or connection closed");
      }
      state = State.ESTABLISHING;
      new Thread(this::threadMain).start();
    }
  }

  /**
   * Sends a clause update over this connection.
   * If an exception is thrown, nothing will be written and the connection is not terminated.
   * The return value indicates if a message has actually been sent or not. There are two cases in
   *     which the message might not be sent. First, there might be an internal socket error. In
   *     this case, onDisconnect() is called. Second, the connection has been terminated. In that
   *     case, onDisconnect() is not called, as it has either been called before or the termination
   *     was initiated using one of the terminate methods (i.e., the user is aware of this).
   * @param c the clause update
   * @return true if sent, false otherwise
   * @throws IllegalStateException if the connection has not been started from the consumer
   * @throws SerializationException if the clause update cannot be serialized
   */
  public boolean sendClauseUpdate(ClauseUpdate c) throws SerializationException {
    // We still need to synchronize here to make sure that no clause updates are sent after a
    // termination message.
    byte type = c.type() == ClauseUpdate.Type.ADD ?
        MessageTypes.CLAUSE_ADD : MessageTypes.CLAUSE_DEL;

    synchronized (SYNC_STATE) {
      switch (state) {
        case INIT, ESTABLISHING, ESTABLISHED -> throw
            new IllegalStateException("terminate before connection is established and started");
        case STARTED -> {
          try {
            client.write(type, c.clause());
            return true;
          } catch (IOException e) { // note: SerializationException does not close this connection
            state = State.CLOSED;
            termMessage = "fail: clause";
            termByte = MessageTypes.TERM_OTHER; // probably not necessary because we failed anyway
            termObject = "fail: clause";
            return false;
          }
        } default -> {
          // case CLOSED
          // no error message, but indication in return value
          return false;
        }
      }
    }
  }

  /**
   * Sends a satisfying variable assignment to the consumer and closes this connection.
   * @param assign the satisfying assignment
   * @throws IllegalStateException if the connection has not been started yet
   */
  public void terminateSolved(SatAssignment assign) {
    synchronized (SYNC_STATE) {
      switch (state) {
        case INIT, ESTABLISHING, ESTABLISHED -> throw
            new IllegalStateException("terminate before connection is established and started");
        case STARTED -> {
          state = State.CLOSED;
          termMessage = null;
          termByte = MessageTypes.TERM_SOLVE;
          termObject = assign;
        }
        // do nothing if state is CLOSED
      }
    }
  }

  /**
   * Sends a refutation message to the consumer and closes this connection.
   * @throws IllegalStateException if the connection has not been started yet
   */
  public void terminateRefuted() {
    synchronized (SYNC_STATE) {
      switch (state) {
        case INIT, ESTABLISHING, ESTABLISHED -> throw
            new IllegalStateException("terminate before connection is established and started");
        case STARTED -> {
          state = State.CLOSED;
          termMessage = null;
          termByte = MessageTypes.TERM_REFUTE;
          termObject = null;
        }
        // do nothing if state is CLOSED
      }
    }
  }

  /**
   * Sends a termination message to the consumer and closes this connection.
   * Note: unlike the other terminate methods, this one may be called at any time.
   * @param reason the reason for termination
   */
  public void terminateOtherwise(String reason) {
    synchronized (SYNC_STATE) {
      switch (state) {
        case INIT -> state = State.CLOSED; // do nothing else
        case ESTABLISHING, ESTABLISHED, STARTED -> {
          state = State.CLOSED;
          termMessage = null;
          termByte = MessageTypes.TERM_OTHER;
          termObject = reason;
        }
        // do nothing if state is CLOSED
      }
    }
  }
}
