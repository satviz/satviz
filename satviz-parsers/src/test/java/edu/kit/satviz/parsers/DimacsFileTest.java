package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class DimacsFileTest {

  InputStream simpleFileStream;
  InputStream longFileStream;
  ClauseUpdate[] simpleFileUpdates = {
          new ClauseUpdate(new Clause(new int[]{1, -3}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{2, 3, -1}), ClauseUpdate.Type.ADD)
  };

  @BeforeEach
  void setUp() {
    simpleFileStream = DimacsFileTest.class.getResourceAsStream("/dimacs_ex/simple_v3_c2.cnf");
    longFileStream = DimacsFileTest.class.getResourceAsStream("/dimacs_ex/aim-100-1_6-no-1.cnf");
  }

  @Test
  void getVariableAmount_test() {
    DimacsFile dimacsFile = new DimacsFile(simpleFileStream);
    assertEquals(3, dimacsFile.getVariableAmount());
  }

  @Test
  void getClauseAmount_test() {
    DimacsFile dimacsFile = new DimacsFile(simpleFileStream);
    assertEquals(2, dimacsFile.getClauseAmount());
  }

  @Test
  void iterator_test() {
    DimacsFile dimacsFile = new DimacsFile(simpleFileStream);
    Iterator<ClauseUpdate> iterator = dimacsFile.iterator();
    for (int i = 0; i < dimacsFile.getClauseAmount(); i++) {
      assertTrue(iterator.hasNext());
      assertEquals(simpleFileUpdates[i], iterator.next());
    }
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

  @Test
  void iterator_long_test() {
    DimacsFile dimacsFile = new DimacsFile(longFileStream);
    Iterator<ClauseUpdate> iterator = dimacsFile.iterator();
    for (int i = 0; i < dimacsFile.getClauseAmount(); i++) {
      assertTrue(iterator.hasNext());
      iterator.next();
    }
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

}
