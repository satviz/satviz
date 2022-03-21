package edu.kit.satviz.common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.tukaani.xz.XZInputStream;

/**
 * Small utility class to help with opening a file
 * and decompressing it if it is marked as compressed.
 */
public final class Compression {

  private Compression() {}

  /**
   * Opens a buffered input stream to the given file,
   * wrapping it in a xz decompression stream if the file name ends with ".xz".
   *
   * @param file The file to open.
   * @return An input stream that can be used to read the file.
   * @throws IOException if an i/o error occurs
   */
  public static InputStream openPossiblyCompressed(Path file) throws IOException {
    InputStream is = new BufferedInputStream(Files.newInputStream(file));
    if (file.getFileName().toString().endsWith(".xz")) {
      is = new XZInputStream(is);
    }
    return is;
  }

}
