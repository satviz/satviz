package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DratFileTest {

  InputStream example4FileStream;
  InputStream example5FileStream;
  ClauseUpdate[] example4Updates = {
          new ClauseUpdate(new Clause(new int[]{-1}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{-1, -2, 3}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{-1, -3, -4}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{-1, 2, 4}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{2}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{}), ClauseUpdate.Type.ADD)
  };

  @BeforeEach
  void setUp() {
    example4FileStream = DimacsFileTest.class.getResourceAsStream("/drat_ex/examples-4-vars.drat");
    example5FileStream = DimacsFileTest.class.getResourceAsStream("/drat_ex/example-5-vars.drat");
  }

  @Test
  void iterator_valid_test() {
    DratFile dratFile = new DratFile(example4FileStream);
    Iterator<ClauseUpdate> iterator = dratFile.iterator();
    for (int i = 0; i < 5; i++) {
      assertTrue(iterator.hasNext());
      assertEquals(example4Updates[i], iterator.next());
    }
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

  @Test
  void iterator_long_test() {
    int addClauseCounter = 0;
    int removeClauseCounter = 0;
    DratFile dratFile = new DratFile(example5FileStream);
    for (ClauseUpdate update : dratFile) {
      if (update.type() == ClauseUpdate.Type.ADD) {
        addClauseCounter++;
      } else if (update.type() == ClauseUpdate.Type.REMOVE) {
        removeClauseCounter++;
      } else {
        fail();
      }
    }
    assertFalse(dratFile.iterator().hasNext());
    assertThrows(NoSuchElementException.class, dratFile.iterator()::next);
    assertEquals(6, addClauseCounter);
    assertEquals(6, removeClauseCounter);
  }

}
