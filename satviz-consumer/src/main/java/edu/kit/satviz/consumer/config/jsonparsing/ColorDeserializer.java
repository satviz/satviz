package edu.kit.satviz.consumer.config.jsonparsing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import javafx.scene.paint.Color;

/**
 * This class is responsible for the deserialization of a hex representation of a color
 * into an instance of the {@link javafx.scene.paint.Color} class.
 */
public class ColorDeserializer extends JsonDeserializer<Color> {

  @Override
  public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonNode node = p.getCodec().readTree(p);
    return Color.web(node.asText());
  }

}
