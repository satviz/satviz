package edu.kit.satviz.sat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.kit.satviz.sat.SatAssignment.VariableState;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * This class tests the functionality of the {@code SatAssignment} class.
 */
class SatAssignmentTest {

  /**
   * This tests, whether invalid parameters result in an
   * {@code IllegalArgumentException} in the constructor.
   */
  @Test
  void constructor_invalidParameters_test() {
    assertThrows(
            IllegalArgumentException.class,
            () -> new SatAssignment(-3)
    );
    assertThrows(
            IllegalArgumentException.class,
            () -> new SatAssignment(0)
    );
  }

  /**
   * This test recursively tests the {@code set()} and {@code get()}
   * for a lot of possible combinations of variables and states.
   */
  @Test
  void set_and_get_test() {
    recursiveTest(4, 4);
  }

  private void recursiveTest(int depth, int varCount) {
    assert depth <= varCount;
    SatAssignment satAssignment = new SatAssignment(varCount);
    int[] intStateArray = new int[varCount];
    for (int i = 0; i < varCount; i++) {
      intStateArray[i] = 0;
    }
    rec(satAssignment, intStateArray, depth, 0);
  }

  private void rec(SatAssignment satAssignment, int[] array, int depth, int current) {
    for (int i = current; i < satAssignment.getVarCount(); i++) {
      for (VariableState state : VariableState.values()) {
        array[i] = SatAssignment.convertVariableStateToIntState(i + 1, state);
        satAssignment.set(i + 1, state);
        if (depth > 1) {
          rec(satAssignment, array, depth - 1, i + 1);
        } else {
          isSatAssignmentEqualToArray(satAssignment, array);
        }
      }
    }
  }

  private void isSatAssignmentEqualToArray(SatAssignment satAssignment, int[] array) {
    assertEquals(array.length, satAssignment.getVarCount());
    for (int i = 0; i < array.length; i++) {
      assertEquals(array[i], satAssignment.getIntState(i + 1));
    }
  }

  /**
   * This tests, whether invalid parameters result in an
   * {@code IllegalArgumentException} in the {@code set()} method.
   */
  @Test
  void set_invalidParameters_test() {
    SatAssignment satAssignment = new SatAssignment(10);
    satAssignment.set(1, VariableState.SET);
    assertThrows(
            IllegalArgumentException.class,
            () -> satAssignment.set(100, VariableState.DONTCARE)
    );
    assertThrows(
            IllegalArgumentException.class,
            () -> satAssignment.set(0, VariableState.DONTCARE)
    );
    assertThrows(
            IllegalArgumentException.class,
            () -> satAssignment.set(3, null)
    );
  }

  /**
   * This tests, whether invalid parameters result in an
   * {@code IllegalArgumentException} in the {@code get()} method.
   */
  @Test
  void get_invalidParameters_test() {
    SatAssignment satAssignment = new SatAssignment(10);
    satAssignment.set(1, VariableState.SET);
    assertThrows(
            IllegalArgumentException.class,
            () -> satAssignment.get(100)
    );
    assertThrows(
            IllegalArgumentException.class,
            () -> satAssignment.get(0)
    );
  }

  /**
   * This tests, whether an instance of {@code SatAssignment} is initialised
   * with {@code DONTCARE} for every variable state.
   */
  @Test
  void get_noAssignment_test() {
    SatAssignment satAssignment = new SatAssignment(10);
    for (int i = 1; i <= satAssignment.getVarCount(); i++) {
      assertEquals(VariableState.DONTCARE, satAssignment.get(i));
    }
  }

  /**
   * This tests, whether invalid parameters result in an {@code IllegalArgumentException}
   * in the {@code convertVariableStateToIntState()} method.
   */
  @Test
  void convertVariableStateToIntState_invalidParameters_test() {
    assertThrows(
            IllegalArgumentException.class,
            () -> SatAssignment.convertVariableStateToIntState(1, null)
    );
    assertThrows(
            IllegalArgumentException.class,
            () -> SatAssignment.convertVariableStateToIntState(0, VariableState.SET)
    );
    assertThrows(
            IllegalArgumentException.class,
            () -> SatAssignment.convertVariableStateToIntState(-10, VariableState.SET)
    );
  }

  /**
   * This tests, whether the {@code fromIntState()} method returns the correct
   * {@code VariableState} for a given integer.
   */
  @Test
  void fromIntState_test() {
    assertEquals(VariableState.DONTCARE, VariableState.fromIntState(0));
    assertEquals(VariableState.SET, VariableState.fromIntState(1));
    assertEquals(VariableState.SET, VariableState.fromIntState(999999));
    assertEquals(VariableState.UNSET, VariableState.fromIntState(-1));
    assertEquals(VariableState.UNSET, VariableState.fromIntState(-7777));
  }

  /**
   * This tests, whether the overridden {@code equals()} method works as intended.
   */
  @Test
  void equals_test() {
    SatAssignment satAssignment1 = new SatAssignment(10);
    satAssignment1.set(3, VariableState.SET);
    satAssignment1.set(4, VariableState.UNSET);

    SatAssignment satAssignment2 = new SatAssignment(10);
    satAssignment2.set(3, VariableState.SET);
    satAssignment2.set(4, VariableState.UNSET);

    SatAssignment satAssignment3 = new SatAssignment(10);
    satAssignment3.set(4, VariableState.UNSET);

    SatAssignment satAssignment4 = new SatAssignment(10);
    satAssignment4.set(3, VariableState.SET);
    satAssignment4.set(4, VariableState.UNSET);
    satAssignment4.set(6, VariableState.SET);

    SatAssignment satAssignment5 = new SatAssignment(9);
    satAssignment5.set(3, VariableState.SET);
    satAssignment5.set(4, VariableState.UNSET);

    assertEquals(satAssignment1, satAssignment1);
    assertEquals(satAssignment1, satAssignment2);
    assertNotEquals(satAssignment1, null);
    assertNotEquals(satAssignment1, new Object());
    assertNotEquals(satAssignment1, satAssignment3);
    assertNotEquals(satAssignment1, satAssignment4);
    assertNotEquals(satAssignment1, satAssignment5);
  }

  /**
   * This tests, whether the overridden {@code hashCode()} method works as intended.
   */
  @Test
  void hashCode_test() {
    SatAssignment satAssignment1 = new SatAssignment(10);
    satAssignment1.set(3, VariableState.SET);
    satAssignment1.set(4, VariableState.UNSET);

    SatAssignment satAssignment2 = new SatAssignment(10);
    satAssignment2.set(3, VariableState.SET);
    satAssignment2.set(4, VariableState.UNSET);

    SatAssignment satAssignment3 = new SatAssignment(10);
    satAssignment3.set(4, VariableState.UNSET);

    Map<SatAssignment, String> statusMap = new HashMap<>();
    statusMap.put(satAssignment1, "status: 1.1");
    assertTrue(statusMap.containsKey(satAssignment2));
    statusMap.put(satAssignment2, "status: 2.1");
    assertEquals("status: 2.1", statusMap.get(satAssignment1));
    statusMap.put(satAssignment3, "status: 3.1");
    assertEquals(2, statusMap.keySet().size());
  }

  /**
   * This tests, whether the overridden {@code toString()} method works as intended.
   */
  @Test
  void toString_test() {
    SatAssignment satAssignment = new SatAssignment(5);
    satAssignment.set(3, VariableState.SET);
    satAssignment.set(4, VariableState.UNSET);

    assertEquals("[DONTCARE, DONTCARE, SET, UNSET, DONTCARE]", satAssignment.toString());
  }

}
