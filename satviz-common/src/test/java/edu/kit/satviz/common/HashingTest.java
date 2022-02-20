package edu.kit.satviz.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class HashingTest {

  @Test
  void test_hashContent_knownHash() throws IOException {
    try (var in = HashingTest.class.getResourceAsStream("/hash_test.txt")) {
      assertEquals(0x1ac2387962102562L, Hashing.hashContent(in));
    }
  }

}
