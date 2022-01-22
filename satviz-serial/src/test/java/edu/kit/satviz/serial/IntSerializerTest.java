package edu.kit.satviz.serial;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntSerializerTest {

  private IntSerializer serializer;
  private int generatedInt;
  private PipedOutputStream out;
  private PipedInputStream in;

  @BeforeEach
  void setUp() throws SerializationException, IOException {
    serializer = new IntSerializer();
    generatedInt = generateNewInteger();
    byte[] buffer = new byte[4];
    in = new PipedInputStream();
    out = new PipedOutputStream(in);
  }

  @Test
  void deserialize_then_serialize_test() throws SerializationException, IOException {
    in.read();
    serializer.serialize(generatedInt, out);
    serializer.deserialize(in);
    assertEquals(generatedInt, in.read());
  }

  private static int generateNewInteger() {
    return ThreadLocalRandom.current().nextInt(0, 1000);
  }

}
