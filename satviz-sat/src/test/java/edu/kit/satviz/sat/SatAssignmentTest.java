package edu.kit.satviz.sat;

import edu.kit.satviz.sat.SatAssignment.VariableState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class tests the functionality of the <code>SatAssignment</code> class.
 *
 * @author quorty
 */
class SatAssignmentTest {

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
   * This tests, whether the error handling of <code>convertVariableStateToIntState()</code> works.
   */
  @Test
  void convertVariableStateToIntState_test() {
    assertEquals(0, SatAssignment.convertVariableStateToIntState(1, null));
  }

}
