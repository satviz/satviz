package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class DimacsFileTest {

  InputStream simpleFileStream;
  InputStream longFileStream;
  InputStream invalidFileStream1;
  InputStream invalidFileStream2;
  InputStream invalidFileStream3;
  ClauseUpdate[] simpleFileUpdates = {
          new ClauseUpdate(new Clause(new int[]{1, -3}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{2, 3, -1}), ClauseUpdate.Type.ADD)
  };

  @BeforeEach
  void setUp() {
    simpleFileStream = DimacsFileTest.class.getResourceAsStream("/dimacs_ex/simple_v3_c2.cnf");
    longFileStream = DimacsFileTest.class.getResourceAsStream("/dimacs_ex/aim-100-1_6-no-1.cnf");
    invalidFileStream1 = DimacsFileTest.class.getResourceAsStream("/dimacs_ex/invalid1-simple_v3_c2.cnf");
    invalidFileStream2 = DimacsFileTest.class.getResourceAsStream("/dimacs_ex/invalid2-simple_v3_c2.cnf");
    invalidFileStream3 = DimacsFileTest.class.getResourceAsStream("/dimacs_ex/invalid3-simple_v3_c2.cnf");
  }

  /**
   * This tests the <code>getVariableAmount</code> method.
   */
  @Test
  void getVariableAmount_test() {
    DimacsFile dimacsFile = new DimacsFile(simpleFileStream);
    assertEquals(3, dimacsFile.getVariableAmount());
  }

  /**
   * This tests the <code>getClauseAmount</code> method.
   */
  @Test
  void getClauseAmount_test() {
    DimacsFile dimacsFile = new DimacsFile(simpleFileStream);
    assertEquals(2, dimacsFile.getClauseAmount());
  }

  /**
   * This tests a valid short CNF file and checks, whether the content is parsed correctly.
   */
  @Test
  void iterator_short_test() {
    DimacsFile dimacsFile = new DimacsFile(simpleFileStream);
    Iterator<ClauseUpdate> iterator = dimacsFile.iterator();
    for (int i = 0; i < dimacsFile.getClauseAmount(); i++) {
      assertTrue(iterator.hasNext());
      assertEquals(simpleFileUpdates[i], iterator.next());
    }
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }

  /**
   * This tests a valid long CNF file (without checking its content).
   */
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

  /**
   * This tests a bunch of illegal headers.
   */
  @Test
  void parseHeader_illegalHeader_test() {
    String header = " cnf 3 3";
    InputStream inputStream1 = new ByteArrayInputStream(header.getBytes());
    assertThrows(ParsingException.class, () -> new DimacsFile(inputStream1));

    header = "p cn 3 3";
    InputStream inputStream2 = new ByteArrayInputStream(header.getBytes());
    assertThrows(ParsingException.class, () -> new DimacsFile(inputStream2));

    header = "p cnf 3";
    InputStream inputStream3 = new ByteArrayInputStream(header.getBytes());
    assertThrows(ParsingException.class, () -> new DimacsFile(inputStream3));

    header = "p cnf -1 -3";
    InputStream inputStream4 = new ByteArrayInputStream(header.getBytes());
    assertThrows(ParsingException.class, () -> new DimacsFile(inputStream4));
  }

  /**
   * This tests a valid header with no clauses.
   */
  @Test
  void iterator_validHeader_test() {
    String header = """
            c gs
            c
            c ag
            
            p cnf 0 0
            """;
    InputStream inputStream1 = new ByteArrayInputStream(header.getBytes());
    assertDoesNotThrow(() -> new DimacsFile(inputStream1));
    InputStream inputStream2 = new ByteArrayInputStream(header.getBytes());
    DimacsFile dimacsFile = new DimacsFile(inputStream2);
    Iterator<ClauseUpdate> iterator = dimacsFile.iterator();
    assertThrows(NoSuchElementException.class, iterator::next);
    assertFalse(iterator.hasNext());
  }

  /**
   * This tests the case, when there's more clauses, than what was said in the header.
   */
  @Test
  void iterator_illegalClauseAmount1_test() {
    DimacsFile dimacsFile = new DimacsFile(invalidFileStream1);
    Iterator<ClauseUpdate> iterator = dimacsFile.iterator();
    assertTrue(iterator.hasNext());
    iterator.next();
    assertThrows(ParsingException.class, iterator::hasNext);
    assertThrows(ParsingException.class, iterator::next);
  }

  /**
   * This tests the case, when there's fewer clauses, than what was said in the header.
   */
  @Test
  void iterator_illegalClauseAmount2_test() {
    DimacsFile dimacsFile = new DimacsFile(invalidFileStream2);
    Iterator<ClauseUpdate> iterator = dimacsFile.iterator();
    assertTrue(iterator.hasNext());
    iterator.next();
    assertTrue(iterator.hasNext());
    iterator.next();
    assertThrows(ParsingException.class, iterator::next);
    assertThrows(ParsingException.class, iterator::hasNext);
  }

  /**
   * This tests the case, when there's a clause, that never terminates with a 0.
   */
  @Test
  void iterator_illegalClauses_test() {
    DimacsFile dimacsFile = new DimacsFile(invalidFileStream3);
    Iterator<ClauseUpdate> iterator = dimacsFile.iterator();
    assertThrows(ParsingException.class, iterator::next);
    assertThrows(ParsingException.class, iterator::hasNext);
    assertThrows(ParsingException.class, iterator::hasNext);
  }

}
