package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.ClauseUpdateSerializer;
import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class stores ClauseUpdates in a temporary file. It allows random access
 * of clause updates at specific indices with the method <code>getClauseUpdate()</code> and adding
 * updates to the end of the file with the method <code>addClauseUpdate()</code>.
 */
public class ExternalClauseBuffer implements AutoCloseable {

  private static final ClauseUpdateSerializer updateSerializer = new ClauseUpdateSerializer();

  private final Object outputLock;
  private final Object readLock;
  private final RandomAccessFile clauseLookupReadFile;
  private final OutputStream clauseLookupOutStream;
  private final RandomAccessFile clauseReadFile;
  private final OutputStream clauseOutStream;
  private final AtomicLong size;
  private final AtomicLong nextClauseBegin;

  public ExternalClauseBuffer(Path dir) throws IOException {
    this.outputLock = new Object();
    this.readLock = new Object();
    Path lookupFile = Files.createTempFile(dir, "satviz-clause-lookup", null);
    Path clauseFile = Files.createTempFile(dir, "satviz-clauses", null);

    this.clauseLookupReadFile = new RandomAccessFile(lookupFile.toFile(), "r");
    this.clauseLookupOutStream = new BufferedOutputStream(Files.newOutputStream(lookupFile));
    this.clauseReadFile = new RandomAccessFile(clauseFile.toFile(), "r");
    this.clauseOutStream = new BufferedOutputStream(Files.newOutputStream(clauseFile));
    this.size = new AtomicLong(0);
    this.nextClauseBegin = new AtomicLong(0);
  }

  public void addClauseUpdate(ClauseUpdate update) throws IOException {
    Objects.requireNonNull(update);
    ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
    try {
      updateSerializer.serialize(update, byteArrayStream);
    } catch (SerializationException e) {
      throw new RuntimeException("Unexpected exception while serializing " + update, e);
    }

    byte[] bytes = byteArrayStream.toByteArray();
    synchronized (outputLock) {
      long clauseBegin = nextClauseBegin.getAndAdd(bytes.length);
      clauseOutStream.write(bytes);
      ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
      buf.putLong(clauseBegin);
      clauseLookupOutStream.write(buf.array());
      size.incrementAndGet();
    }
  }

  public ClauseUpdate[] getClauseUpdates(long index, int numUpdates)
      throws IOException, SerializationException {
    synchronized (outputLock) {
      flush();
    }
    synchronized (readLock) {
      int actualUpdates = (int) Math.min(numUpdates, size() - index);
      long beginningByte = getBeginningByte(index);
      clauseReadFile.seek(beginningByte);
      ClauseUpdate[] updates = new ClauseUpdate[actualUpdates];
      SerialBuilder<ClauseUpdate> builder = updateSerializer.getBuilder();
      for (int i = 0; i < actualUpdates; i++) {
        while (!builder.finished()) {
          builder.addByte(clauseReadFile.readByte());
        }
        updates[i] = builder.getObject();
        builder.reset();
      }
      return updates;
    }
  }

  private void flush() throws IOException {
    clauseLookupOutStream.flush();
    clauseOutStream.flush();
  }

  private long getBeginningByte(long index) throws IOException {
    clauseLookupReadFile.seek(index * Long.BYTES);
    byte[] array = new byte[Long.BYTES];
    clauseLookupReadFile.read(array);
    return ByteBuffer.wrap(array).getLong();
  }

  public long size() {
    return size.get();
  }

  @Override
  public void close() throws IOException {
    synchronized (outputLock) {
      clauseLookupOutStream.close();
      clauseOutStream.close();
    }

    synchronized (readLock) {
      clauseLookupReadFile.close();
      clauseReadFile.close();
    }
  }

}
