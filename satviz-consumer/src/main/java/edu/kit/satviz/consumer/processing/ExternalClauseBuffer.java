package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.ClauseUpdateSerializer;
import edu.kit.satviz.serial.SerializationException;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class stores ClauseUpdates in a temporary file. It allows random access
 * of clause updates at specific indices with the method <code>getClauseUpdate()</code> and adding
 * updates to the end of the file with the method <code>addClauseUpdate()</code>.
 */
public class ExternalClauseBuffer implements AutoCloseable {

  private static final ClauseUpdateSerializer updateSerializer = new ClauseUpdateSerializer();

  private final Lock outputLock;
  private final Lock readLock;
  private final RandomAccessFile clauseLookupReadFile;
  private final OutputStream clauseLookupOutStream;
  private final RandomAccessFile clauseReadFile;
  private final OutputStream clauseOutStream;

  private volatile long size;
  private long nextClauseBegin;

  public ExternalClauseBuffer(Path dir) throws IOException {
    this.outputLock = new ReentrantLock();
    this.readLock = new ReentrantLock();
    Path lookupFile = Files.createTempFile(dir, "satviz-clause-lookup", null);
    Path clauseFile = Files.createTempFile(dir, "satviz-clauses", null);

    this.clauseLookupReadFile = new RandomAccessFile(lookupFile.toFile(), "r");
    this.clauseLookupOutStream = new BufferedOutputStream(Files.newOutputStream(lookupFile));
    this.clauseReadFile = new RandomAccessFile(clauseFile.toFile(), "r");
    this.clauseOutStream = new BufferedOutputStream(Files.newOutputStream(clauseFile));
    this.size = 0;
    this.nextClauseBegin = 0;
    writeInitialClauseBegin();
  }

  private void writeInitialClauseBegin() throws IOException {
    clauseLookupOutStream.write(new byte[Long.BYTES]);
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
    ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
    outputLock.lock();
    try {
      clauseOutStream.write(bytes);
      buf.putLong(nextClauseBegin);
      nextClauseBegin += bytes.length;
      clauseLookupOutStream.write(buf.array());
      size++;
    } finally {
      outputLock.unlock();
    }
  }

  public ClauseUpdate[] getClauseUpdates(long index, int numUpdates)
      throws IOException, SerializationException {
    outputLock.lock();
    try {
      flush();
    } finally {
      outputLock.unlock();
    }

    readLock.lock();
    try {
      int actualNumUpdates = (int) Math.min(numUpdates, size - index);
      byte[] bytePositions = new byte[(actualNumUpdates + 1) * Long.BYTES];
      clauseLookupReadFile.seek(index * Long.BYTES);
      clauseLookupReadFile.read(bytePositions);
      ByteBuffer buffer = ByteBuffer.wrap(bytePositions);
      ClauseUpdate[] updates = new ClauseUpdate[actualNumUpdates];
      for (int i = 1; i <= actualNumUpdates; i++) {
        long beginningByte = buffer.getLong(i - 1);
        long endingByte = buffer.getLong(i);
        int clauseSize = (int) (endingByte - beginningByte);
        byte[] clauseBuf = new byte[clauseSize];
        clauseReadFile.seek(beginningByte);
        clauseReadFile.read(clauseBuf);
        updates[i - 1] = updateSerializer.deserialize(new ByteArrayInputStream(clauseBuf));
      }
      return updates;
    } finally {
      readLock.unlock();
    }
  }

  private void flush() throws IOException {
    clauseLookupOutStream.flush();
    clauseOutStream.flush();
  }

  public long size() {
    return size;
  }

  @Override
  public void close() throws IOException {
    outputLock.lock();
    readLock.lock();
    try {
      clauseLookupOutStream.close();
      clauseOutStream.close();
      clauseLookupReadFile.close();
      clauseReadFile.close();
    } finally {
      readLock.unlock();
      outputLock.unlock();
    }
  }

}
