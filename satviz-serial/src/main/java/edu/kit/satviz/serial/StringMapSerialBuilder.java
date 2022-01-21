package edu.kit.satviz.serial;

import java.util.HashMap;
import java.util.Map;

public class StringMapSerialBuilder extends SerialBuilder<Map<String, String>> {
  private final StringSerialBuilder builder = new StringSerialBuilder();
  private boolean currentEmpty = true;
  private boolean buildingKey = true;
  private String currentKey;
  private final Map<String, String> map = new HashMap<>();

  private boolean failed = false;
  private boolean done = false;

  @Override
  public boolean addByte(byte b) throws SerializationException {
    if (done || failed) {
      throw new SerializationException("done");
    }

    switch (b) {
      case (byte) '=' -> {
        if (!buildingKey || currentEmpty) {
          failed = true;
          throw new SerializationException("unexpected value assignment");
        }
        builder.addByte((byte) '\0');
        currentKey = builder.getObject();
        builder.reset();
        currentEmpty = true;
        buildingKey = false;
        return false;
      }
      case (byte) '\n', (byte) '\0' -> {
        if (buildingKey || currentEmpty) {
          failed = true;
          throw new SerializationException("unexpected end of key-value pair");
        }
        builder.addByte((byte) '\0');
        map.put(currentKey, builder.getObject());
        if (b == (byte) '\n') {
          builder.reset();
          currentEmpty = true;
          buildingKey = true;
          return false;
        } else {
          done = true;
          return true;
        }
      }
      default -> {
        builder.addByte(b);
        return false;
      }
    }
  }

  @Override
  public boolean objectFinished() {
    return done;
  }

  @Override
  public Map<String, String> getObject() {
    return done ? null : map;
  }
}
