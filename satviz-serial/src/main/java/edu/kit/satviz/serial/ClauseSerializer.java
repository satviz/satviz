package edu.kit.satviz.serial;

import edu.kit.satviz.sat.Clause;

import java.io.IOException;
import java.io.OutputStream;

public class ClauseSerializer extends Serializer<Clause> {

  @Override
  public void serialize(Clause clause, OutputStream out) throws IOException, SerializationException {
    int[] literals = clause.literals();
    for (int i = 0; i < literals.length; i++) {
      int lit = literals[i];
      int ui = (i > 0) ? 2 * lit : (-2 * lit) + 1;
      while (ui > 0x7f) {
        out.write((ui & 0x7f) | 0x80);
        ui >>>= 7;
      }
      out.write(ui);
    }
    out.write(0);
  }

  @Override
  public SerialBuilder<Clause> getBuilder() {
    return new ClauseSerialBuilder();
  }
}
