package edu.kit.satviz.serial;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link SerialBuilder} for string key-value maps.
 *
 * @author luwae
 */
public class StringMapSerialBuilder extends SerialBuilder<Map<String, String>> {
  private final StringSerialBuilder builder = new StringSerialBuilder();
  private boolean currentEmpty = true;
  private boolean buildingKey = true;
  private String currentKey = null;
  private final Map<String, String> map = new HashMap<>();

  @Override
  protected void processAddByte(byte b) throws SerializationException {
    switch (b) {
      case (byte) '=' -> {
        if (!buildingKey || currentEmpty) {
          fail("unexpected value assign");
        }
        builder.addByte((byte) '\0');
        currentKey = builder.getObject();
        builder.reset();
        currentEmpty = true;
        buildingKey = false;
      }
      case (byte) '\n', (byte) '\0' -> {
        if (buildingKey || currentEmpty) {
          fail("unexpected key-value pair ending");
        }
        builder.addByte((byte) '\0');
        map.put(currentKey, builder.getObject());
        if (b == (byte) '\n') {
          builder.reset();
          currentEmpty = true;
          buildingKey = true;
        } else {
          finish();
        }
      }
      default -> {
        builder.addByte(b);
        currentEmpty = false;
      }
    }
  }

  @Override
  protected Map<String, String> processGetObject() {
    return map;
  }

  @Override
  protected void processReset() {
    builder.reset();
    currentEmpty = true;
    buildingKey = true;
    currentKey = null;
    map.clear();
  }
}
