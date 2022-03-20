package edu.kit.satviz.network;

import edu.kit.satviz.network.general.NetworkBlueprint;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.serial.ClauseSerializer;
import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkBlueprintTest {
  private static Map<Byte, Serializer<?>> m;
  private static NetworkBlueprint bp;
  private static final Clause c = new Clause(new int[]{1, 2, 3, 4, 5, -1, 1000000, -1000000});

  @BeforeAll
  static void beforeAll() {
    m = new HashMap<>();
    m.put((byte) 42, new ClauseSerializer());
    bp = new NetworkBlueprint(m);
  }

  @Test
  void testGet() {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try {
      bp.serialize((byte) 42, c, byteOut);
    } catch (IOException | SerializationException e) {
      fail(e);
    }

    SerialBuilder<?> builder = bp.getBuilder((byte) 42);
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
    assertEquals(c, builder.getObject());
  }

  @Test
  void testError() {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    // try invalid byte
    assertThrows(SerializationException.class, () -> bp.serialize((byte) 43, c, byteOut));
    // try invalid object for given byte
    assertThrows(SerializationException.class, () -> bp.serialize((byte) 42, bp, byteOut));

    assertNull(bp.getBuilder((byte) 43));
  }
}
