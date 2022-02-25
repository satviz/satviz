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
 * A memory-external, append-only, ranged random access storage for {@link ClauseUpdate}s.
 *
 * <p>This class uses temporary files to store its data.
 *
 * @implNote Currently, the implementation uses two temporary files: One that contains all the
 *           added clause updates, serialised in order, and one that acts as a lookup table.
 *           The lookup table file contains the byte at which each clause begins in the clause file.
 *           Hence, whenever a clause is added, it is first serialised to the clause file, then
 *           its beginning byte number is written to the lookup file.
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

  /**
   * Create and initialise a new {@code ExternalClauseBuffer} with no initial clauses.
   *
   * @param dir The directory where the data will be stored.
   * @throws IOException if an I/O error occurs.
   */
  public ExternalClauseBuffer(Path dir) throws IOException {
    this.outputLock = new ReentrantLock();
    this.readLock = new ReentrantLock();
    Path lookupFile = Files.createTempFile(dir, "satviz-clause-lookup", null);
    Path clauseFile = Files.createTempFile(dir, "satviz-clauses", null);
    lookupFile.toFile().deleteOnExit();
    clauseFile.toFile().deleteOnExit();
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

  /**
   * Add a clause update to this buffer.
   *
   * @param update the {@link ClauseUpdate}.
   * @throws IOException if the update can't be stored due to an I/O error.
   */
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
      nextClauseBegin += bytes.length;
      buf.putLong(nextClauseBegin);
      clauseLookupOutStream.write(buf.array());
      size++;
    } finally {
      outputLock.unlock();
    }
  }

  /**
   * Get a number of clause updates starting from a specific index.
   *
   * <p>This method is <em>lenient</em>: If the {@code index} is valid but the number of clauses
   * requested is more than this buffer holds after the index, it reads the maximum amount of clause
   * updates possible.<br>
   * E.g., for {@link #size()} {@code == 4}, {@code index == 1} and {@code numUpdates == 6},
   * 3 clause updates will be read.
   *
   * @param index The index of the first clause
   * @param numUpdates The number of clause updates to read, starting from {@code index}
   * @return An array of {@link ClauseUpdate}s. As explained in the summary, it is not guaranteed to
   *         be of length {@code numUpdates}.
   * @throws IOException if an I/O error occurs.
   * @throws SerializationException If a clause update cannot be deserialised.
   *                                This can only happen if the files used in this implementation
   *                                are modified from the outside.
   * @throws IndexOutOfBoundsException if {@code index >=} {@link #size()} or {@code index < 0}
   * @throws IllegalArgumentException if {@code numUpdates < 0}
   */
  public ClauseUpdate[] getClauseUpdates(long index, int numUpdates)
      throws IOException, SerializationException {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("For clause update index " + index);
    }
    if (numUpdates < 0) {
      throw new IllegalArgumentException("Number of updates must be non-negative");
    }
    outputLock.lock();
    try {
      flush();
    } finally {
      outputLock.unlock();
    }

    readLock.lock();
    try {
      int actualNumUpdates = (int) Math.min(numUpdates, size - index);
      ClauseUpdate[] updates = new ClauseUpdate[actualNumUpdates];
      // TODO: 08/02/2022 pick strategy
      readUpdatesWithEphemeralBuffers(index, updates);
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
      long beginningByte = buffer.getLong((i - 1) * Long.BYTES);
      long endingByte = buffer.getLong(i * Long.BYTES);
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

  /**
   * Returns the number of clause updates stored in this buffer.
   *
   * @return the size of this buffer.
   */
  public long size() {
    return size;
  }

  /**
   * Closes this buffer.<br>
   * After performing this operation, this buffer must not be used anymore.
   *
   * <p>Note: this <strong>does not</strong> delete the temporary files created by this buffer.
   *
   * @throws IOException if an I/O error occurs.
   */
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
