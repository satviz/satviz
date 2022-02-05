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

  @Test
  void iterator_validHeader_test() {
    String header = """
            c gs
            c
            c ag
            
            p cnf 0 0
            """;
    InputStream inputStream = new ByteArrayInputStream(header.getBytes());
    assertDoesNotThrow(() -> new DimacsFile(inputStream));
  }

  @Test
  void iterator_illegalClauseAmount_test() {
    DimacsFile dimacsFile = new DimacsFile(invalidFileStream1);
    Iterator<ClauseUpdate> iterator = dimacsFile.iterator();
    assertTrue(iterator.hasNext());
    iterator.next();
    assertFalse(iterator.hasNext());
    assertThrows(ParsingException.class, iterator::next);
  }

  @Test
  void iterator_illegalClauses_test() {
    DimacsFile dimacsFile = new DimacsFile(invalidFileStream2);
    Iterator<ClauseUpdate> iterator = dimacsFile.iterator();
    assertThrows(ParsingException.class, iterator::next);
    assertFalse(iterator.hasNext());
  }

}
