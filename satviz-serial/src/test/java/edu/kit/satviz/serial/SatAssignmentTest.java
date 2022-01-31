package edu.kit.satviz.serial;

import edu.kit.satviz.sat.SatAssignment;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SatAssignmentTest {

  private final SatAssignmentSerializer serial = new SatAssignmentSerializer();

  @Test
  void testEmpty() {
    SatAssignment empty = new SatAssignment(10);
    assertDoesNotThrow(() -> testSingleAssignment(empty));
  }

  @Test
  void testOneSet() {
    SatAssignment assign = new SatAssignment(100);
    assign.set(42, SatAssignment.VariableState.SET);
    assign.set(43, SatAssignment.VariableState.UNSET);
    assign.set(44, SatAssignment.VariableState.RESERVED);
    assertDoesNotThrow(() -> testSingleAssignment(assign));
  }

  void testSingleAssignment(SatAssignment assign) throws IOException, SerializationException {
    byte[] bytes = new byte[assign.getVarCount()]; // quick upper bound, doesn't matter
    ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream(bytes);

    serial.serialize(assign, byteOut);
    SatAssignment result = serial.deserialize(byteIn);
    assertEquals(result, assign); // .equals() implemented in SatAssignment
  }
}
