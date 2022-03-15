package edu.kit.satviz.sat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClauseTest {

  /**
   * This tests, whether the overridden {@code equals()} method works as intended.
   */
  @Test
  void equals_test() {
    Clause clause1 = new Clause(new int[] {2, 3});
    Clause clause2 = new Clause(new int[] {2, 3});
    Clause clause3 = new Clause(new int[] {2, 3, 4});

    assertEquals(clause1, clause1);
    assertEquals(clause1, clause2);
    assertNotEquals(clause1, null);
    assertNotEquals(clause1, new Object());
    assertNotEquals(clause1, clause3);
  }

  /**
   * This tests, whether the overridden {@code hashCode()} method works as intended.
   */
  @Test
  void hashCode_test() {
    Clause clause1 = new Clause(new int[] {2, 3});
    Clause clause2 = new Clause(new int[] {2, 3});
    Clause clause3 = new Clause(new int[] {2, 3, 4});

    Map<Clause, String> statusMap = new HashMap<>();
    statusMap.put(clause1, "status: 1.1");
    assertTrue(statusMap.containsKey(clause2));
    statusMap.put(clause2, "status: 2.1");
    assertEquals("status: 2.1", statusMap.get(clause1));
    statusMap.put(clause3, "status: 3.1");
    assertEquals(2, statusMap.keySet().size());
  }

  /**
   * This tests, whether the overridden {@code toString()} method works as intended.
   */
  @Test
  void toString_test() {
    Clause clause = new Clause(new int[] {-2, 33, 0});

    assertEquals("[-2, 33, 0]", clause.toString());
  }

}
