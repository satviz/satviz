package edu.kit.satviz.consumer.bindings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utility class to load shared libraries from the resources of this application.
 */
public final class NativeLibraryLoader {

  private NativeLibraryLoader() {

  }

  /**
   * Loads a shared library located at the given resource path by copying it to a temporary file
   * and using {@link System#load(String)}.
   *
   * @param path The path to the shared library file.
   *             Relative resource paths will be resolved from this class' package.
   * @throws IOException If the resource can't be copied to an actual file.
   */
  public static void loadLibrary(String path) throws IOException {
    Path file = Files.createTempFile("satviz-lib", null);
    try (InputStream is = NativeLibraryLoader.class.getResourceAsStream(path)) {
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
    }
    System.load(file.toAbsolutePath().toString());
  }

}
