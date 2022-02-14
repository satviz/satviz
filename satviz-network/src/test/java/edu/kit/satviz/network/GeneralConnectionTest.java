package edu.kit.satviz.network;

import edu.kit.satviz.serial.Serializer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GeneralConnectionTest {
  private static final int PORT = 35124;

  @Test
  void test() {
    Map<Byte, Serializer<?>> m = new HashMap<>();
    m.put((byte) 1, new NullSerializer());
    m.put((byte) 2, new NullSerializer());

    ServerConnectionManager conman = new ServerConnectionManager(PORT, new NetworkBlueprint(m));
    assertTrue(conman.start());
    assertFalse(conman.start());
    assertFalse(conman.isClosed());

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // ok
    }

    assertDoesNotThrow(conman::stop);
    assertTrue(conman.isClosed());
  }

}
