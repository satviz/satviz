package edu.kit.satviz.consumer.bindings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class NativeLibraryLoader {

  private NativeLibraryLoader() {

  }

  public static void loadLibrary(String path) throws IOException {
    Path file = Files.createTempFile("satviz-lib", null);
    try (InputStream is = NativeLibraryLoader.class.getResourceAsStream(path)) {
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
    }
    System.load(file.toAbsolutePath().toString());
  }

}
