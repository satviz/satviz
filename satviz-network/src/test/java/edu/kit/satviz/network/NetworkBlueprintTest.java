package edu.kit.satviz.network;

import edu.kit.satviz.network.general.NetworkBlueprint;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.serial.ClauseSerializer;
import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkBlueprintTest {

  @Test
  void testGet() {
    Map<Byte, Serializer<?>> m = new HashMap<>();
    m.put((byte) 42, new ClauseSerializer());
    NetworkBlueprint bp = new NetworkBlueprint(m);

    Clause c = new Clause(new int[]{1, 2, 3, 4, 5, -1, 1000000, -1000000});
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try {
      bp.serialize((byte) 42, c, byteOut);
    } catch (IOException | SerializationException e) {
      fail(e);
    }

    SerialBuilder<?> builder = bp.getBuilder(42);
    assertNotNull(builder);
    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
    int read = byteIn.read();
    while(read != -1) {
      try {
        builder.addByte((byte) read);
      } catch (SerializationException e) {
        fail(e);
      }
      read = byteIn.read();
    }
    assertNotNull(builder.getObject());
    assertEquals(c, (Clause) builder.getObject());
  }
}
