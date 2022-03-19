package edu.kit.satviz.consumer.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IdentityMappingTest {

  @Test
  void test_mapping() {
    var m = IdentityMapping.INSTANCE;
    assertEquals(0, m.applyAsInt(1));
    assertEquals(32, m.applyAsInt(33));
    assertEquals(6, m.applyAsInt(-7));
  }

}
