package edu.kit.satviz.network.general;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * A client connection to send and receive {@link NetworkMessage}s.
 * Reading is done asynchronously, while writing is done synchronously.
 * This is a wrapper around {@link SocketChannel}.
 */
public class Connection implements AutoCloseable {
  private final SocketChannel chan;
  private final NetworkBlueprint bp;
  private byte currentType;
  private SerialBuilder<?> currentBuilder = null;
  private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);

  private boolean readingFailed = false;
  private boolean writingFailed = false;
  private final Object SYNC_READ = new Object();
  private final Object SYNC_WRITE = new Object();

  /**
   * Creates a new connection by opening a socket channel and connecting to the specified address.
   * Throws {@link ConnectException} if the connection is refused remotely, i.e., no-one is
   * listening on the remote port.
   *
   * @param address the remote address
   * @param port    the remote port
   * @param bp      the types of messages
   * @throws IOException if an I/O error occurs
   */
  public Connection(String address, int port, NetworkBlueprint bp) throws IOException {
    this.bp = bp;
    this.chan = SocketChannel.open();
    this.chan.configureBlocking(false);
    if (!this.chan.connect(new InetSocketAddress(address, port))) {
      this.chan.finishConnect();
    }
  }

  /**
   * Creates a new connection with an already connected socket.
   *
   * @param chan the socket channel
   * @param bp   the types of messages
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
   *
   * @return remote address, {@code null} if not connected
   * @throws ClosedChannelException if the channel is closed
   * @throws IOException            if an I/O error occurs
   */
  public InetSocketAddress getRemoteAddress() throws IOException {
    return (InetSocketAddress) chan.getRemoteAddress();
  }

  /**
   * Registers this channel with the given selector.
   *
   * @param sel the selector
   * @param ops the interest set
   * @throws ClosedChannelException if the channel is closed
   */
  public void register(Selector sel, int ops) throws ClosedChannelException {
    chan.register(sel, ops);
  }

  private NetworkMessage processByte(byte b) throws SerializationException {
    if (currentBuilder == null) {
      currentType = b;
      currentBuilder = bp.getBuilder(b);
      if (currentBuilder == null) { // didn't get builder
        readingFailed = true;
        throw new SerializationException("no builder available for type " + b);
      }
      return null;
    }

    boolean done;
    try {
      done = currentBuilder.addByte(b);
    } catch (SerializationException e) {
      readingFailed = true;
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
   * Only processes bytes that are available immediately, which means the sequence might be empty.
   * If a serialization error occurs, subsequent calls to this method will always throw a
   * {@link SerializationException}. This does not affect writing, and it does not close the
   * underlying socket.
   * This method is thread-safe; concurrent calls will always block until the pending read
   * operation is complete.
   *
   * @return sequence of messages in a queue
   * @throws IOException            if an I/O error occurs
   * @throws SerializationException if the incoming bytes do not encode valid messages
   */
  public Queue<NetworkMessage> read() throws IOException, SerializationException {
    synchronized (SYNC_READ) {
      if (readingFailed) {
        throw new SerializationException("failed previously");
      }

      readBuffer.clear();
      int numBytesAhead = chan.read(readBuffer);
      readBuffer.flip();

      Queue<NetworkMessage> messages = new ArrayDeque<>();
      while (numBytesAhead-- > 0) { // if chan.read() returned 0 or -1 we do nothing
        NetworkMessage msg = processByte(readBuffer.get());
        if (msg != null) {
          messages.add(msg);
        }
      }
      return messages;
    }
  }

  /**
   * Writes a {@link NetworkMessage} to this connection.
   * Writing is synchronous, which means that either the entire message is written or an exception
   * is thrown.
   * If a serialization error occurs, subsequent calls to this method will always throw a
   * {@link SerializationException}. This does not affect reading, and it does not close the
   * underlying socket.
   * This method is thread-safe; concurrent calls will always block until the pending write
   * operation is complete.
   *
   * @param type the message type
   * @param obj  the message object
   * @throws IOException            if an I/O error occurs
   * @throws SerializationException if the message cannot be encoded for this connection
   */
  public void write(byte type, Object obj) throws IOException, SerializationException {
    synchronized (SYNC_WRITE) {
      if (writingFailed) {
        throw new SerializationException("failed previously");
      }

      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      byteOut.write(type);
      try {
        bp.serialize(type, obj, byteOut);
      } catch (SerializationException e) {
        writingFailed = true;
        throw e;
      }
      ByteBuffer writeBuffer = ByteBuffer.wrap(byteOut.toByteArray());

      while (writeBuffer.hasRemaining()) { // force synchronous
        // if another thread calls close(), this method may throw
        // ClosedChannelException or AsynchronousCloseException
        chan.write(writeBuffer);
      }
    }
  }

  /**
   * Closes this channel.
   * Calling this method may cause concurrent reads or writes to fail.
   */
  public void close() {
    try {
      chan.close();
    } catch (Exception e) {
      // do nothing more; don't propagate exceptions to the outside
      // shutdown should not throw exceptions
    }
  }
}