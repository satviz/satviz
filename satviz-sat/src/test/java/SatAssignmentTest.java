import edu.kit.satviz.sat.SatAssignment;
import edu.kit.satviz.sat.SatAssignment.VariableState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class tests the functionality of the SatAssignment class.
 *
 * @author quorty
 */
class SatAssignmentTest {

  /**
   * This test recursively tests all possible combinations of variables and states.
   */
  @Test
  void setTest() {
    recursive_set_test(4, 4);
  }

  private void recursive_set_test(int depth, int varCount) {
    assert depth <= varCount;
    SatAssignment satAssignment = new SatAssignment(varCount);
    int[] intStateArray = new int[varCount+1];
    for (int i = 0; i < varCount+1; i++) {
      intStateArray[i] = 0;
    }
    rec_set(satAssignment, intStateArray, depth, 1);
  }

  private void rec_set(SatAssignment satAssignment, int[] array, int depth, int current) {
    for (int i = current; i <= satAssignment.getVarCount(); i++) {
      for (byte j = 3; j >= 0; j--) {
        array[i] = SatAssignment.convertVariableStateToIntState(i, VariableState.fromValue(j));
        satAssignment.set(i, VariableState.fromValue(j));
        if (depth > 1) {
          rec_set(satAssignment, array, depth - 1, i + 1);
        } else {
          isSatAssignmentEqualToArray(satAssignment, array);
        }
      }
    }
  }

  private void isSatAssignmentEqualToArray(SatAssignment satAssignment, int[] array) {
    assertEquals(array.length - 1, satAssignment.getVarCount());
    for (int i = 0; i < array.length; i++) {
      assertEquals(array[i], satAssignment.getIntState(i));
    }
  }

}
