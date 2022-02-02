package edu.kit.satviz.serial;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link SerialBuilder} for string key-value maps.
 */
public class StringMapSerialBuilder extends SerialBuilder<Map<String, String>> {
  private final StringSerialBuilder builder = new StringSerialBuilder();
  private boolean currentEmpty = true;
  private boolean buildingKey = true;
  private boolean escaped = false;
  private String currentKey = null;
  private final Map<String, String> map = new HashMap<>();

  @Override
  protected void processAddByte(byte b) throws SerializationException {
    if (escaped) {
      // add the character, no matter what it was
      escaped = false;
      addToString(b);
      return;
    }

    switch (b) {
      case (byte) '\\' -> escaped = true;
      case (byte) '=' -> {
        if (!buildingKey || currentEmpty) {
          fail("unexpected value assign");
        }
        currentKey = finishString();
        resetBuilder(false);
      }
      case (byte) '\n', (byte) '\0' -> {
        if (buildingKey || currentEmpty) {
          fail("unexpected key-value pair ending");
        }
        map.put(currentKey, finishString());
        if (b == (byte) '\n') {
          resetBuilder(true);
        } else {
          finish();
        }
      }
      default -> addToString(b);
    }
  }

  private void resetBuilder(boolean toKey) {
    builder.reset();
    currentEmpty = true;
    buildingKey = toKey;
  }

  private void addToString(byte b) throws SerializationException {
    builder.addByte(b);
    currentEmpty = false;
  }

  private String finishString() throws SerializationException {
    builder.addByte((byte) '\0');
    return builder.getObject();
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
    escaped = false;
    currentKey = null;
    map.clear();
  }
}
