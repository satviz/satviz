package edu.kit.satviz.network.general;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * A client connection to send and receive {@link NetworkMessage}s.
 * Reading is done asynchronously, while writing is done synchronously to ensure that
 *     messages are sent in their entirety.
 * This is a wrapper around {@link SocketChannel}.
 */
public class Connection {
  private final SocketChannel chan;
  private final NetworkBlueprint bp;
  private byte currentType;
  private SerialBuilder<?> currentBuilder = null;
  private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);
  private boolean failed = false;

  /**
   * Creates a new connection by opening a socket channel and connecting to the specified address.
   * Throws {@link ConnectException} if the connection is refused remotely, i.e., no-one is
   *     listening on the remote port.
   * @param address the remote address
   * @param port the remote port
   * @param bp the types of messages
   * @throws IOException if an I/O error occurs
   */
  public Connection(String address, int port, NetworkBlueprint bp) throws IOException {
    this.bp = bp;
    this.chan = SocketChannel.open();
    this.chan.configureBlocking(false);
    this.chan.connect(new InetSocketAddress(address, port));
  }

  /**
   * Creates a new connection with an already connected socket.
   * @param chan the socket channel
   * @param bp the types of messages
   * @throws IllegalArgumentException if the channel is blocking or not connected
   */
  public Connection(SocketChannel chan, NetworkBlueprint bp) {
    this.bp = bp;
    if (chan.isBlocking() || !chan.isConnected()) {
      throw new IllegalArgumentException("blocking or not connected socket channel");
    }
    this.chan = chan;
  }

  /**
   * Returns the remote address.
   * @return remote address
   * @throws ClosedChannelException if the channel is closed
   * @throws IOException if an I/O error occurs
   */
  public InetSocketAddress getRemoteAddress() throws IOException {
    return (InetSocketAddress) chan.getRemoteAddress();
  }

  /**
   * Registers this channel with the given selector.
   * @param sel the selector
   * @param ops the interest set
   * @throws ClosedChannelException if the channel is closed
   */
  public void register(Selector sel, int ops) throws ClosedChannelException {
    chan.register(sel, ops);
  }

  /**
   * Closes this channel.
   */
  public void close() {
    try {
      chan.close();
    } catch (Exception e) {
      // do nothing more; don't propagate exceptions to the outside
      // shutdown should not throw exceptions
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

  /**
   * Reads a sequence of {@link NetworkMessage}s from this connection asynchronously.
   * The sequence might be empty.
   * If a serialization error occurs, subsequent calls to <code>read()</code> will always
   *     throw a {@link SerializationException}, which means no further messages can be read.
   * This does not affect writing, and it does not close the underlying socket.
   * @return sequence of messages in a queue
   * @throws IOException if an I/O error occurs
   * @throws SerializationException if the incoming bytes do not encode valid messages
   */
  public Queue<NetworkMessage> read() throws IOException, SerializationException {
    if (failed) {
      // a fail in reading doesn't affect socket writing
      throw new SerializationException("failed previously");
    }

    readBuffer.clear();
    int numBytesAhead = chan.read(readBuffer);
    readBuffer.flip();
    if (numBytesAhead == -1) { // closed socket means we cannot read anything
      numBytesAhead = 0;
    }

    Queue<NetworkMessage> messages = new ArrayDeque<>();
    while (numBytesAhead-- > 0) {
      NetworkMessage msg = processByte(readBuffer.get());
      if (msg != null) {
        messages.add(msg);
      }
    }
    return messages;
  }

  /**
   * Writes a {@link NetworkMessage} to this connection.
   * If a serialization error occurs, no bytes will be written. Further messages can be written;
   *     the underlying socket is not closed.
   * @param type the message type
   * @param obj the message object
   * @throws IOException if an I/O error occurs
   * @throws SerializationException if the message cannot be encoded for this connection
   */
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