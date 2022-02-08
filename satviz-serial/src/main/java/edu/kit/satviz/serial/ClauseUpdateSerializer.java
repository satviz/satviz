package edu.kit.satviz.serial;

import edu.kit.satviz.sat.ClauseUpdate;
import java.io.IOException;
import java.io.OutputStream;

public class ClauseUpdateSerializer extends Serializer<ClauseUpdate> {
  @Override
  public void serialize(ClauseUpdate clauseUpdate, OutputStream out)
      throws IOException, SerializationException {

  }

  @Override
  public SerialBuilder<ClauseUpdate> getBuilder() {
    return null;
  }
}
