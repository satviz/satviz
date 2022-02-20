package edu.kit.satviz.sat;

import edu.kit.satviz.sat.SatAssignment.VariableState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This class tests the functionality of the <code>SatAssignment</code> class.
 */
class SatAssignmentTest {

  /**
   * This tests, whether invalid parameters result in an
   * <code>IllegalArgumentException</code> in the constructor.
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
   * This test recursively tests the <code>set()</code> and <code>get()</code>
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
   * <code>IllegalArgumentException</code> in the <code>set()</code> method.
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
   * <code>IllegalArgumentException</code> in the <code>get()</code> method.
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
   * This tests, whether an instance of <code>SatAssignment</code> is initialised
   * with <code>DONTCARE</code> for every variable state.
   */
  @Test
  void get_noAssignment_test() {
    SatAssignment satAssignment = new SatAssignment(10);
    for (int i = 1; i <= satAssignment.getVarCount(); i++) {
      assertEquals(VariableState.DONTCARE, satAssignment.get(i));
    }
  }

  /**
   * This tests, whether invalid parameters result in an <code>IllegalArgumentException</code>
   * in the <code>convertVariableStateToIntState()</code> method.
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

}
