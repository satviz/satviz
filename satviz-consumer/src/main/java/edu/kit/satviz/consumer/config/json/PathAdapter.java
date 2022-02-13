package edu.kit.satviz.consumer.config.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This TypeAdapter adapts the Path class, so that the gson parser can
 * serialize and deserialize instances of the Path class without issues.
 *
 * @author johnnyjayjay
 */
public class PathAdapter extends TypeAdapter<Path> {

  @Override
  public void write(JsonWriter out, Path value) throws IOException {
    if (value != null) {
      out.value(value.toString());
    }
  }

  @Override
  public Path read(JsonReader in) throws IOException {
    return Paths.get(in.nextString());
  }

}
