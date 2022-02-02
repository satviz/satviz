package edu.kit.satviz.serial;

import edu.kit.satviz.sat.Clause;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A {@link Serializer} for SAT clauses.
 * This class uses the variable-length binary DRAT format.
 *
 * @author luwae
 */
public class ClauseSerializer extends Serializer<Clause> {

  @Override
  public void serialize(Clause clause, OutputStream out) throws IOException {
    int[] literals = clause.literals();
    for (int lit : literals) {
      int unsignedMapping = (lit > 0) ? 2 * lit : (-2 * lit) + 1;
      // split into 7 bit blocks
      while (unsignedMapping > 0x7f) {
        out.write((unsignedMapping & 0x7f) | 0x80);
        unsignedMapping >>>= 7;
      }
      out.write(unsignedMapping); // last byte has MSB 0
    }
    // need to write trailing 0 manually
    out.write(0);
  }

  @Override
  public SerialBuilder<Clause> getBuilder() {
    return new ClauseSerialBuilder();
  }
}
