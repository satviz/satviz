package edu.kit.satviz.sat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * This class tests the functionality of the {@code ClauseUpdate} class.
 */
class ClauseUpdateTest {

  /**
   * This tests, whether the static {@code of()} method works as an alternative
   * to creating an instance of the record manually.
   */
  @Test
  void of_test() {
    int[] literals = new int[] {1, 2, 3};
    Clause clause = new Clause(literals);
    ClauseUpdate update = new ClauseUpdate(clause, ClauseUpdate.Type.ADD);

    assertEquals(update, ClauseUpdate.of(ClauseUpdate.Type.ADD, 1, 2, 3));
  }

  // Tests for the nested enum Type

  /**
   * This tests, whether each instance of the enum returns the correct ID when calling {@code getId()}.
   */
  @Test
  void getId_test() {
    assertEquals('a', ClauseUpdate.Type.ADD.getId());
    assertEquals('d', ClauseUpdate.Type.REMOVE.getId());
  }

  /**
   * This tests, whether entering valid IDs into the static {@code getById()} method
   * does actually create valid instances of the enum.
   */
  @Test
  void getById_valid_test() {
    assertEquals(ClauseUpdate.Type.ADD, ClauseUpdate.Type.getById((byte) 'a'));
    assertEquals(ClauseUpdate.Type.REMOVE, ClauseUpdate.Type.getById((byte) 'd'));
  }

  /**
   * This tests, whether entering invalid IDs into the static {@code getById()} method
   * throws a {@code NoSuchElementException}.
   */
  @Test
  void getById_illegalId_test() {
    assertThrows(NoSuchElementException.class, () -> ClauseUpdate.Type.getById((byte) 'r'));
    assertThrows(NoSuchElementException.class, () -> ClauseUpdate.Type.getById((byte) '\0'));
  }

}
