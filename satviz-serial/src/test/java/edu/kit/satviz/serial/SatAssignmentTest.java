package edu.kit.satviz.serial;

import edu.kit.satviz.sat.SatAssignment;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SatAssignmentTest {

  private final SatAssignmentSerializer serial = new SatAssignmentSerializer();

  @Test
  void testEmpty() throws IOException {
    SatAssignment empty = new SatAssignment(10);
    try {
      testSingleAssignment(empty);
    } catch (SerializationException e) {
      fail(e);
    }
  }

  @Test
  void testOneSet() throws IOException {
    SatAssignment assign = new SatAssignment(100);
    assign.set(42, SatAssignment.VariableState.SET);
    assign.set(43, SatAssignment.VariableState.UNSET);
    assign.set(44, SatAssignment.VariableState.RESERVED);
    try {
      testSingleAssignment(assign);
    } catch (SerializationException e) {
      fail(e);
    }
  }

  void testSingleAssignment(SatAssignment assign) throws IOException, SerializationException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    serial.serialize(assign, byteOut);

    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
    SatAssignment result = serial.deserialize(byteIn);

    assertEquals(assign, result); // .equals() implemented in SatAssignment
  }

  @Test
  void testReset() {
    SatAssignment assign = new SatAssignment(10);
    assign.set(5, SatAssignment.VariableState.SET);
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try {
      serial.serialize(assign, byteOut);
    } catch (IOException e) {
      fail(e);
    }

    SatAssignmentSerialBuilder builder = new SatAssignmentSerialBuilder();
    try {
      assertFalse(builder.addByte((byte) 42));
      // don't want to use this byte
      builder.reset();
      for(byte b : byteOut.toByteArray()) {
        builder.addByte(b);
      }
      SatAssignment assign2 = builder.getObject();
      assertNotNull(assign2);
      assertEquals(assign, assign2);

    } catch (SerializationException e) {
      fail(e);
    }
  }
}
