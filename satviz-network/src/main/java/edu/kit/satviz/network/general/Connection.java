package edu.kit.satviz.network.general;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Queue;

public class Connection {
  private final SocketChannel chan;
  private final NetworkBlueprint bp;
  private byte currentType;
  private SerialBuilder<?> currentBuilder = null;
  private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);
  private int numBytesAhead = 0;
  private boolean failed = false;

  public Connection(String address, int port, NetworkBlueprint bp) throws IOException {
    this.bp = bp;
    this.chan = SocketChannel.open();
    this.chan.configureBlocking(false);
    this.chan.connect(new InetSocketAddress(address, port));
  }

  public Connection(SocketChannel chan, NetworkBlueprint bp) {
    this.bp = bp;
    this.chan = chan;
  }

  public InetSocketAddress getRemoteAddress() throws IOException {
    return (InetSocketAddress) chan.getRemoteAddress();
  }

  public void close() throws IOException {
    chan.close();
  }

  public boolean failed() {
    return failed;
  }

  private void fillBuffer() throws IOException {
    readBuffer.clear();
    numBytesAhead = chan.read(readBuffer);
    readBuffer.flip();
    if (numBytesAhead == -1) { // closed socket means we cannot read anything
      numBytesAhead = 0;
    }
  }

  private NetworkMessage processByte(byte b) throws SerializationException {
    if (currentBuilder == null) {
      currentType = b;
      currentBuilder = bp.getBuilder(b);
      if (currentBuilder == null) { // didn't get builder
        failed = true;
        throw new SerializationException("no builder available for type " + b);
      }
      return null;
    }

    boolean done;
    try {
      done = currentBuilder.addByte(b);
    } catch (SerializationException e) {
      failed = true;
      throw e;
    }

    if (done) {
      NetworkMessage msg = new NetworkMessage(currentType, currentBuilder.getObject());
      currentBuilder = null; // remove last builder
      return msg;
    }
    return null;
  }

  public Queue<NetworkMessage> read() throws IOException, SerializationException {
    if (failed) {
      throw new SerializationException("failed previously");
    }

    Queue<NetworkMessage> messages = new ArrayDeque<>();

    fillBuffer();

    while (numBytesAhead-- > 0) {
      NetworkMessage msg = processByte(readBuffer.get());
      if (msg != null) {
        messages.add(msg);
      }
    }
    return messages;
  }

  public void write(byte type, Object obj) throws IOException, SerializationException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    byteOut.write(type);
    bp.serialize(type, obj, byteOut);
    ByteBuffer writeBuffer = ByteBuffer.wrap(byteOut.toByteArray());

    while (writeBuffer.hasRemaining()) { // force synchronous
      chan.write(writeBuffer);
    }
  }
}