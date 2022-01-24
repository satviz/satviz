package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClientConnectionManager {
  private enum Status {
    NEW,
    STARTED,
    FINISHED,
    FAILED,
  }
  private static final int READ_CAP = 1024;

  private final NetworkBlueprint bp;
  private final ConnectionContext context;

  private Consumer<ConnectionId> lsConn;
  private Consumer<String> lsFail;

  private Status status = Status.NEW;

  public ClientConnectionManager(String address, int port, NetworkBlueprint bp) throws IOException {
    InetSocketAddress addr = new InetSocketAddress(address, port);
    this.context = new ConnectionContext(new ConnectionId(addr), new Receiver(bp::getBuilder), null);
    this.bp = bp;
  }

  private void fail(String reason) {
    if (status == Status.FAILED) {
      return;
    }
    status = Status.FAILED;
    lsFail.accept(reason);
    context.close();
  }

  private void doStart() {
    while (true) {
      try {
        context.tryConnect();
      } catch (IOException e) {
        fail("connection");
        break;
      }
      if (context.isConnected()) {
        break;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // just continue
      }
    }

    // actual work
    ByteBuffer bb = ByteBuffer.allocate(READ_CAP);
    while (status == Status.STARTED) {
      // Read from socket and put in receiver
      try {
        context.readAndConvert(bb);
      } catch (IOException e) {
        status = Status.FAILED;
        return; // is some error handling missing here?
      }
      bb.clear();
    }
  }

  public void start() {
    if (status != Status.NEW) {
      throw new IllegalStateException("already started");
    }
    status = Status.STARTED;
    new Thread(
        this::doStart
    ).start();
  }

  private void stop() {
    status = (status == Status.FAILED) ? Status.FAILED : Status.FINISHED;
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

  public void send(byte type, Object obj) throws SerializationException, IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    byteOut.write(type);
    bp.serialize(type, obj, byteOut);
    ByteBuffer bb = ByteBuffer.wrap(byteOut.toByteArray());
    context.write(bb);
  }
}
