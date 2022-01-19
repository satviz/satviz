import edu.kit.satviz.sat.SatAssignment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SatAssignmentTest {

  @Test
  void setValueTest() {
    recursive_setValue_test(4, 4);
  }

  private void recursive_setValue_test(int depth, int varCount) {
    assert depth <= varCount;
    SatAssignment satAssignment = new SatAssignment(varCount);
    int[] array = new int[varCount];
    for (int i = 0; i < varCount; i++) {
      array[i] = 0;
    }
    rec_setValue(satAssignment, array, depth, 0);
  }

  private void rec_setValue(SatAssignment satAssignment, int[] array, int depth, int current) {
    for (int i = current + 1; i < satAssignment.getVarCount(); i++) {
      for (byte j = 0; j < 4; j++) {
        array[i] = j;
        satAssignment.setVal(i, j);
        if (depth > 1) {
          rec_setValue(satAssignment, array, --depth, i);
        } else {
          isSatAssignmentEqualToArray(satAssignment, array);
        }
      }
    }
  }

  private void isSatAssignmentEqualToArray(SatAssignment satAssignment, int[] array) {
    assertEquals(array.length, satAssignment.getVarCount());
    for (int i = 0; i < array.length; i++) {
      assertEquals(array[i], satAssignment.getVal(i));
    }
  }

}
