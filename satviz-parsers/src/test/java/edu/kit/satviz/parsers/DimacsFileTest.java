package edu.kit.satviz.parsers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DimacsFileTest {

  InputStream in;

  @BeforeEach
  void setUp() {
    in = DimacsFileTest.class.getResourceAsStream("/dimacs_ex/simple_v3_c2.cnf");
  }

  @Test
  void getVariableAmount_test() {
    DimacsFile simpleFile = new DimacsFile(in);
    assertEquals(3, simpleFile.getVariableAmount());
  }

  @Test
  void getClauseAmount_test() {
    DimacsFile simpleFile = new DimacsFile(in);
    assertEquals(2, simpleFile.getClauseAmount());
  }

}
