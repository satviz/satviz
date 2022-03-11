package edu.kit.satviz.network.pub;

import edu.kit.satviz.network.general.Connection;
import edu.kit.satviz.network.general.NetworkMessage;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

/**
 * The producer part of a satviz network connection.
 * State changes are communicated via the {@link ProducerConnectionListener}. {@code onConnect} is
 *     called once the consumer sends the START signal. {@code onDisconnect} has the following
 *     contract: it is only called if {@code onConnect} has been called before, and it is only
 *     called if the user did not use one of the terminate methods (i.e., it is only called if a
 *     STOP signal was received, or on an internal error).
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

  /**
   * Creates a new connection to a consumer.
   * {@code pid} and {@code ls} should not be {@code null}.
   * @param address the consumer address
   * @param port the consumer port
   * @param pid the type of clause source that this producer represents
   * @param ls the event listener
   */
  public ProducerConnection(String address, int port, ProducerId pid, ProducerConnectionListener ls) {
    this.address = address;
    this.port = port;
    this.pid = Objects.requireNonNull(pid);
    this.ls = Objects.requireNonNull(ls);
  }

  /**
   * Closes the underlying connection, optionally sending a last message to the consumer, and
   *     making an {@code onDisconnect()} call.
   * Calling {@code onDisconnect} and sending a last message are independent; you may do only one
   *    of the two.
   * This method only performs its actions exactly once; subsequent calls do nothing.
   * @param onDisconnectMessage the {@code onDisconnect} message, {@code null} if not desired
   * @param type the type of the last message, 0 if not desired
   * @param obj the object of the last message (may be {@code null})
   * @return whether the last message was sent or not
   */
  private boolean doClose(String onDisconnectMessage, byte type, Object obj) {
    synchronized (SYNC_STATE) {
      if (state == State.CLOSED) {
        return false;
      }
      state = State.CLOSED;

      boolean sent = false;
      if (client != null) {
        if (type != 0) {
          sent = true;
          try {
            client.write(type, obj);
          } catch (Exception e) {
            // ignore because there is nothing we can do now
            sent = false;
          }
        }
        client.close();
      }

      if (onDisconnectMessage != null && state == State.STARTED) {
        // only send onDisconnect if onConnect has been sent previously
        // TODO both should come from the same thread
        ls.onDisconnect(onDisconnectMessage);
      }
      return sent;
    }
  }

  private boolean doFail(String msg) {
    return doClose(msg, MessageTypes.TERM_OTHER, msg);
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
      offerData.put("hash", Long.toString(solverId.getInstanceHash()));
      offerData.put("delayed", solverId.isSolverDelayed() ? "true" : "false");
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
          // we do not expect to be woken up here, but spurious wake-ups don't matter
          try {
            SYNC_STATE.wait(1000);
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            doFail("interrupted");
            return false;
          }
        } catch (Exception e) {
          doFail("connection create fail");
          return false;
        }
      }

      if (client == null) { // state must be State.CLOSED
        return false;
      }

      try {
        client.write(MessageTypes.OFFER, offerData);
      } catch (Exception e) {
        doFail("offer send fail");
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
      doFail("selector init fail");
      return;
    }

    Queue<NetworkMessage> readQueue;
    while (true) {
      try {
        sel.select(1000); // avoid busy-wait
        sel.selectedKeys().clear(); // act like we took care of everything
        readQueue = client.read();
      } catch (ClosedChannelException e) { // includes AsynchronousCloseException
        // someone called close; we don't need to do that here
        return;
      } catch (Exception e) {
        doFail("read fail");
        return;
      }

      for (NetworkMessage msg: readQueue) {
        switch (msg.getType()) {
          case MessageTypes.START -> {
            synchronized (SYNC_STATE) {
              if (state == State.CLOSED) {
                return;
              }
              state = State.STARTED;
              ls.onConnect();
            }
          }
          case MessageTypes.STOP -> {
            doClose("stop", (byte) 0, null);
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
    // Note: I thought about using something like a ReadWriteLock here to allow more than one
    // concurrent clause writer, but I decided against it since all writes happen in serial order
    // in the Connection anyway.
    // We still need to synchronize here to make sure that no clause updates are sent after a
    // termination message. Most likely the monitor is free, as the only other synchronization-
    // expensive operation (establishing) has finished already.
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
            doClose("fail: clause", MessageTypes.TERM_OTHER, "fail: clause");
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
   * @return whether the assignment was sent or not
   * @throws IllegalStateException if the connection has not been started yet
   */
  public boolean terminateSolved(SatAssignment assign) {
    synchronized (SYNC_STATE) {
      switch (state) {
        case INIT, ESTABLISHING, ESTABLISHED -> throw
            new IllegalStateException("terminate before connection is established and started");
        default -> {
          return doClose(null, MessageTypes.TERM_SOLVE, assign);
        }
      }
    }
  }

  /**
   * Sends a refutation message to the consumer and closes this connection.
   * @return whether the message was sent or not
   * @throws IllegalStateException if the connection has not been started yet
   */
  public boolean terminateRefuted() {
    synchronized (SYNC_STATE) {
      switch (state) {
        case INIT, ESTABLISHING, ESTABLISHED -> throw
            new IllegalStateException("terminate before connection is established and started");
        default -> {
          return doClose(null, MessageTypes.TERM_REFUTE, null);
        }
      }
    }
  }

  /**
   * Sends a termination message to the consumer and closes this connection.
   * Note: unlike the other terminate methods, this one may be called at any time.
   * @param reason the reason for termination
   * @return whether the message was sent or not
   */
  public boolean terminateOtherwise(String reason) {
    return doClose(null, MessageTypes.TERM_OTHER, reason);
  }
}
