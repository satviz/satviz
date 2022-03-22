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
    // TODO: 22.03.22 probably not the right way to do this
    gen.writeString("#" + String.format("%08X", value.hashCode()));
  }

}
