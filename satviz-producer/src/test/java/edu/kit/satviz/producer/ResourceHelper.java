package edu.kit.satviz.producer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ResourceHelper {

  public static Path extractResource(String name) throws IOException {
    var file = Files.createTempFile("test-resource", null);
    try (var is = ResourceHelper.class.getResourceAsStream(name)) {
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
    }
    return file;
  }

}
