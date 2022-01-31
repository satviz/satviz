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
  ClauseUpdate[] simpleFileUpdates = {
          new ClauseUpdate(new Clause(new int[]{1, -3}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{2, 3, -1}), ClauseUpdate.Type.ADD)
  };

  @BeforeEach
  void setUp() {
    simpleFileStream = DimacsFileTest.class.getResourceAsStream("/dimacs_ex/simple_v3_c2.cnf");
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
    assertTrue(iterator.hasNext());
    assertEquals(simpleFileUpdates[0], iterator.next());
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

}
