package edu.kit.satviz.serial;

import edu.kit.satviz.sat.ClauseUpdate;
import java.io.IOException;
import java.io.OutputStream;

public class ClauseUpdateSerializer extends Serializer<ClauseUpdate> {

  private static final ClauseSerializer clauseSerializer = new ClauseSerializer();

  @Override
  public void serialize(ClauseUpdate clauseUpdate, OutputStream out)
      throws IOException, SerializationException {
    out.write(clauseUpdate.type().ordinal());
    clauseSerializer.serialize(clauseUpdate.clause(), out);
  }

  @Override
  public SerialBuilder<ClauseUpdate> getBuilder() {
    return new ClauseUpdateSerialBuilder();
  }
}
