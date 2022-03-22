package edu.kit.satviz.consumer.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ArrayNodeMappingTest {

  @Test
  void test_mapping() {
    int[] a = {2, 4, 1, 9, 4, 0};
    var m = new ArrayNodeMapping(a);
    assertEquals(2, m.applyAsInt(1));
    assertEquals(1, m.applyAsInt(-3));
    assertEquals(0, m.applyAsInt(6));
  }
}
