package edu.kit.satviz.network.pub;

import edu.kit.satviz.network.general.Connection;
import edu.kit.satviz.network.general.NetworkMessage;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import edu.kit.satviz.serial.SerializationException;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

public class ProducerConnection {
  private enum State {
    INIT,
    ESTABLISHING,
    ESTABLISHED,
    STARTED,
    CLOSING,
    CLOSED
  }

  private final String address;
  private final int port;
  private Connection client = null;
  private final ProducerId pid;
  private final ProducerConnectionListener ls;

  private final Object SYNC_STATE = new Object();
  private volatile State state = State.INIT;

  public ProducerConnection(String address, int port, ProducerId pid, ProducerConnectionListener ls) {
    this.address = address;
    this.port = port;
    this.pid = pid;
    this.ls = Objects.requireNonNull(ls);
  }

  private void doClose(String onDisconnectMessage, byte type, Object msg) {
    synchronized (SYNC_STATE) {
      if (state == State.CLOSED) {
        return;
      }
      state = State.CLOSED;

      if (client != null) {
        if (type != 0) {
          try {
            client.write(type, msg);
          } catch (Exception e) {
            // ignore because there is nothing we can do now
          }
        }
        client.close();
      }

      if (onDisconnectMessage != null) {
        ls.onDisconnect(onDisconnectMessage);
      }
    }
  }

  private void doFail() {
    doClose("internal failure", MessageTypes.TERM_FAIL, "internal failure");
  }

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
            doFail();
            return false;
          }
        } catch (Exception e) {
          doFail();
          return false;
        }
      }

      if (client == null) { // got signal from user that we should close
        doClose(null, MessageTypes.TERM_FAIL, "closed");
        return false;
      }

      try {
        client.write(MessageTypes.OFFER, offerData);
      } catch (Exception e) {
        doFail();
        return false;
      }

      state = State.ESTABLISHED;
      return true;
    }
  }

  private void threadMain() {
    if (!doEstablish()) {
      // state is set to closed and connection is closed
      return;
    }

    // Note: we cannot have the read() call in a synchronized block, as this would interfere
    // with writing. Waiting for up to one second in read() while we cannot write is not desirable.
    // This means we have to be careful here, as we may read past a close(). Sometimes this is
    // unavoidable, and not indicative of a synchronization mistake.

    Queue<NetworkMessage> readQueue;
    while (true) {
      synchronized (SYNC_STATE) {
        if (state == State.CLOSING) {
          doClose(null, MessageTypes.TERM_FAIL, "closed");
          return;
        }
      }

      try {
        readQueue = client.read();
      } catch (ClosedChannelException e) { // includes AsynchronousCloseException
        // someone called close; we don't need to do that here
        return;
      } catch (IOException | SerializationException e) {
        doFail();
        return;
      }

      for (NetworkMessage msg: readQueue) {
        switch (msg.getType()) {
          case MessageTypes.START -> {
            synchronized (SYNC_STATE) {
              if (state == State.CLOSING || state == State.CLOSED) {
                doClose(null, MessageTypes.TERM_FAIL, "closed");
                return;
              }
              state = State.STARTED;
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

  public void sendClauseUpdate(ClauseUpdate c) throws SerializationException {
    // Note: I thought about using something like a ReadWriteLock here to allow more than one
    // concurrent clause writer, but I decided against it since all writes happen in serial order
    // in the Connection anyway.
    // We still need to synchronize here to make sure that no clause updates are sent after a
    // termination message. Most likely the monitor is free, as the only other synchronization-
    // expensive operation (establishing) has finished already.
    byte type = c.type() == ClauseUpdate.Type.ADD ?
        MessageTypes.CLAUSE_ADD : MessageTypes.CLAUSE_DEL;

    synchronized (SYNC_STATE) {
      if (state != State.STARTED) {
        // do something. Don't throw an exception, because we may not be able to avoid
        // clauses being written after close() is called. We usually just want to ignore them.
        // but the user needs some kind of signal if something went wrong
        // TODO isn't this exactly what the onDisconnect is for?
        // TODO make sure that onDisconnect is not ignored, if you use it (2nd call to doClose)
      }
      try {
        client.write(type, c.clause());
      } catch (IOException e) {
        doFail();
      }
      // note: socket is not closed on a SerializationException
    }
  }

  public void terminateSolved(SatAssignment assign) {
    synchronized (SYNC_STATE) {
      if (state == State.INIT || state == State.ESTABLISHING || state == State.ESTABLISHED) {
        throw new IllegalStateException("terminate before connection is established and started");
      }
      if (state == State.STARTED) {
        doClose(null, MessageTypes.TERM_SOLVE, assign); // TODO remove send there
      }
      // otherwise we do nothing
      // TODO don't we want the reader thread to handle termination?
      // TODO if so, do we even want direct doClose() calls in any of these methods?

      // TODO are there different conventions for terminateFailed and the others?
    }
  }

  public void terminateRefuted() {
    synchronized (SYNC_STATE) {
      // TODO
    }
  }

  public void terminateOtherwise(String reason) {
    synchronized (SYNC_STATE) {
      // TODO
    }
  }
}
