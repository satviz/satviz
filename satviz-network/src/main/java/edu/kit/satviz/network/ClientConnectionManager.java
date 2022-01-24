package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClientConnectionManager {
  private enum State {
    NEW,
    STARTED,
    FAILED,
  }

  private static final int READ_CAP = 1024;

  private final NetworkBlueprint bp;
  private final ConnectionContext context;

  private Consumer<ConnectionId> lsConn;
  private Consumer<String> lsFail;

  private State state;

  public ClientConnectionManager(String address, int port, NetworkBlueprint bp) {
    InetSocketAddress addr = new InetSocketAddress(address, port);
    this.context = new ConnectionContext(new ConnectionId(addr), new Receiver(bp::getBuilder), null);
    this.bp = bp;
    this.state = State.NEW;
  }

  private void doStart() {
    boolean connected = false;
    while (!connected) {
      try {
        connected = context.tryConnect();
      } catch (IOException e) {
        // TODO fatal fail
        break;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // just continue? TODO
      }
    }
    state = State.STARTED; // TODO sync
    lsConn.accept(context.getCid());

    // actual work
    ByteBuffer bb = ByteBuffer.allocate(READ_CAP);
    while (state == State.STARTED) {
      // Read from socket and put in receiver
      try {
        context.read(bb);
      } catch (IOException e) {
        // TODO fatal error
      }
    }
  }

  public void start() throws IllegalStateException {
    if (state != State.NEW) {
      throw new IllegalStateException("already started");
    }

    state = State.STARTED;
    new Thread(
        this::doStart
    ).start();
  }

  private void stop() {
    if (state != State.FAILED) {
      state = State.FINISHED; // TODO synchronize here if the inner thread also closes

    }
  }

  public void registerConnect(Consumer<ConnectionId> ls) {
    this.lsConn = ls;
  }

  public void registerGlobalFail(Consumer<String> ls) {
    this.lsFail = ls;
  }

  public void register(BiConsumer<ConnectionId, NetworkMessage> ls) {
    context.setListener(ls);
  }

  public void send(byte type, Object obj) throws IOException {
    if (!context.isConnected()) {
      // TODO make sure this also includes state == FAILED
      throw new NotYetConnectedException();
    }

    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    byteOut.write(type);
    try {
      bp.serialize(type, obj, byteOut);
    } catch (SerializationException | IOException | ClassCastException e) {
      // something went wrong that we can't correct
      context.close(true); // only fail locally
      throw new IOException("couldn't turn this object into a message");
    }
    ByteBuffer bb = ByteBuffer.wrap(byteOut.toByteArray());
    context.write(bb);
  }
}
