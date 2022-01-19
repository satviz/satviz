package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NetworkBlueprintTest {
  private static class TestSerializer extends Serializer<Object> {

    @Override
    public void serialize(Object o, OutputStream out) {
      // stub
    }

    @Override
    public Object deserialize(InputStream in) {
      return null;
    }

    @Override
    public SerialBuilder<Object> getBuilder() {
      return new NullSerialBuilder();
    }
  }

  private final static TestSerializer ts = new TestSerializer();
  private static NetworkBlueprint bp;

  @BeforeAll
  static void setup() {
    Map<Byte, Serializer<?>> m = new HashMap<>();
    m.put((byte) 3, ts);
    bp = new NetworkBlueprint(m);
  }

  @Test
  void mappingWorks() {
    assertEquals(bp.getSerializer((byte) 3), ts);
    assertNotNull(bp.getBuilder(3));
  }

  @Test
  void unspecifiedMappingWorks() {
    assertNull(bp.getSerializer((byte) 2));
    assertNotNull(bp.getBuilder(2));
  }
}
