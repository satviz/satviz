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
  private ByteArrayOutputStream out;
  private ByteArrayInputStream in;

  @BeforeEach
  void setUp() throws SerializationException, IOException {
    serializer = new IntSerializer();
    generatedInt = generateNewInteger();
    byte[] array = new byte[4];
    out = new ByteArrayOutputStream(array);
    in = new ByteArrayInputStream(array);
  }

  @Test
  void deserialize_then_serialize_test() throws SerializationException, IOException {
    serializer.serialize(generatedInt, out);
    assertEquals(generatedInt, serializer.deserialize(in));
  }

  private static int generateNewInteger() {
    return ThreadLocalRandom.current().nextInt(0, 1000);
  }

}
