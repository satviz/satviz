package edu.kit.satviz.consumer.config.jsonparsing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import javafx.scene.paint.Color;

/**
 * This class is responsible for the serialization of an instance of the
 * {@link javafx.scene.paint.Color} class into a hex representation.
 */
public class ColorSerializer extends JsonSerializer<Color> {

  @Override
  public void serialize(Color value, JsonGenerator gen, SerializerProvider sp) throws IOException {
    gen.writeString("#" + addPrecedingZeroes(Integer.toHexString(value.hashCode())));
  }

  private String addPrecedingZeroes(String hexString) {
    return switch (hexString.length()) {
      case 2 -> "000000";
      case 3 -> String.format("00000%s", hexString.substring(0, 1));
      case 4 -> String.format("0000%s", hexString.substring(0, 2));
      case 5 -> String.format("000%s", hexString.substring(0, 3));
      case 6 -> String.format("00%s", hexString.substring(0, 4));
      case 7 -> String.format("0%s", hexString.substring(0, 5));
      default -> hexString.substring(0, 6);
    };
  }

}
