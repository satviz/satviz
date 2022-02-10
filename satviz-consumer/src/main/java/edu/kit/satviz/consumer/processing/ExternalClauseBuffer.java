package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.ClauseUpdateSerializer;
import edu.kit.satviz.serial.SerialBuilder;
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
      if (actualNumUpdates < 0) {
        throw new IndexOutOfBoundsException("For clause update index " + index);
      }
      ClauseUpdate[] updates = new ClauseUpdate[actualNumUpdates];
      // TODO: 08/02/2022 pick strategy
      readUpdatesBufferless(index, updates);
      return updates;
    } finally {
      readLock.unlock();
    }
  }

  // read clause updates from the given index byte by byte, without any buffering.
  private void readUpdatesBufferless(long index, ClauseUpdate[] updates)
      throws IOException, SerializationException {
    clauseLookupReadFile.seek(index * Long.BYTES);
    byte[] array = new byte[Long.BYTES];
    clauseLookupReadFile.read(array);
    long beginningByte = ByteBuffer.wrap(array).getLong();
    clauseReadFile.seek(beginningByte);
    SerialBuilder<ClauseUpdate> builder = updateSerializer.getBuilder();
    for (int i = 0; i < updates.length; i++) {
      while (!builder.finished()) {
        builder.addByte(clauseReadFile.readByte());
      }
      updates[i] = builder.getObject();
      builder.reset();
    }
  }

  // read clause updates from given index, reading each entire clause at once
  private void readUpdatesWithEphemeralBuffers(long index, ClauseUpdate[] updates)
      throws IOException, SerializationException {
    byte[] bytePositions = new byte[(updates.length + 1) * Long.BYTES];
    clauseLookupReadFile.seek(index * Long.BYTES);
    clauseLookupReadFile.read(bytePositions);
    ByteBuffer buffer = ByteBuffer.wrap(bytePositions);
    for (int i = 1; i <= updates.length; i++) {
      long beginningByte = buffer.getLong(i - 1);
      long endingByte = buffer.getLong(i);
      int clauseSize = (int) (endingByte - beginningByte);
      byte[] clauseBuf = new byte[clauseSize];
      clauseReadFile.seek(beginningByte);
      clauseReadFile.read(clauseBuf);
      updates[i - 1] = updateSerializer.deserialize(new ByteArrayInputStream(clauseBuf));
    }
  }

  // read clause updates from given index, reading the entire data all at once
  private void readUpdatesWithBigBuffer(long index, ClauseUpdate[] updates)
      throws IOException, SerializationException {
    byte[] byteRange = new byte[2 * Long.BYTES];
    clauseLookupReadFile.seek(index * Long.BYTES);
    clauseLookupReadFile.read(byteRange, 0, Long.BYTES);
    clauseLookupReadFile.seek((index + updates.length) * Long.BYTES);
    clauseLookupReadFile.read(byteRange, Long.BYTES, 2 * Long.BYTES);
    ByteBuffer buffer = ByteBuffer.wrap(byteRange);
    long beginningByte = buffer.getLong();
    long endingByte = buffer.getLong();
    byte[] clauseUpdateBytes = new byte[(int) (endingByte - beginningByte)];
    clauseReadFile.seek(beginningByte);
    clauseReadFile.read(clauseUpdateBytes);
    ByteArrayInputStream stream = new ByteArrayInputStream(clauseUpdateBytes);
    for (int i = 0; i < updates.length; i++) {
      updates[i] = updateSerializer.deserialize(stream);
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
