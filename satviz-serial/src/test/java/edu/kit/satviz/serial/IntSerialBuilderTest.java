package edu.kit.satviz.serial;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class IntSerialBuilderTest {

  private int generatedForFull;

  private IntSerialBuilder emptyIntBuilder;
  private IntSerialBuilder fullIntBuilder;

  @BeforeEach
  void setUp() throws SerializationException {
    emptyIntBuilder = new IntSerialBuilder();

    generatedForFull = generateNewInteger();
    fullIntBuilder = new IntSerialBuilder();
    for (int i = 0; i < 4; i++) {
      fullIntBuilder.addByte((byte) (generatedForFull >>> (8 * i)));
    }
  }

  @Test
  void addByte_successful_test() throws SerializationException {
    int generatedNumber = generateNewInteger();

    for (int i = 0; i < 4; i++) {
      emptyIntBuilder.addByte((byte) (generatedNumber >>> (8 * i)));
    }
    assertEquals(generatedNumber, emptyIntBuilder.getObject().intValue());
  }

  @Test
  void addByte_finished_test() {
    int generatedNumber = generateNewInteger();

    assertThrows(SerializationException.class, () -> {
      fullIntBuilder.addByte((byte) generatedNumber);
    });
  }

  @Test
  void finished_notFinished_test() throws SerializationException {
    int generatedNumber = generateNewInteger();

    for (int i = 0; i < 4; i++) {
      assertFalse(emptyIntBuilder.finished());
      emptyIntBuilder.addByte((byte) (generatedNumber >>> (8 * i)));
    }
  }

  @Test
  void finished_finished_test() {
    assertTrue(fullIntBuilder.finished());
  }

  @Test
  void getObject_notFinished_test() throws SerializationException {
    int generatedNumber = generateNewInteger();

    for (int i = 0; i < 4; i++) {
      assertNull(emptyIntBuilder.getObject());
      emptyIntBuilder.addByte((byte) (generatedNumber >>> (8 * i)));
    }
  }

  @Test
  void getObject_finished_test() {
    int val = fullIntBuilder.getObject().intValue();
    assertEquals(generatedForFull, val);
  }

  @Test
  void reset_test() {
    int generatedNumber = generateNewInteger();

    fullIntBuilder.reset();

    assertFalse(fullIntBuilder.finished());
    assertNull(fullIntBuilder.getObject());
    assertDoesNotThrow(() -> {
      emptyIntBuilder.addByte((byte) generatedNumber);
    });
  }

  private static int generateNewInteger() {
    return ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

}
