package edu.kit.satviz.producer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class ResourceHelper {

  private static Path tempDir;

  public static void createTempDir() throws IOException {
    tempDir = Files.createTempDirectory("producer-test");
  }

  public static void deleteTempDir() throws IOException {
    Files.walkFileTree(tempDir, new DeletingVisitor());
  }

  public static Path extractResource(String name) throws IOException {
    var file = tempDir == null
        ? Files.createTempFile("test-resource", null)
        : Files.createTempFile(tempDir, null, null);
    try (var is = ResourceHelper.class.getResourceAsStream(name)) {
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
    }
    return file;
  }

  private static class DeletingVisitor extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      Files.delete(file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      Files.delete(dir);
      return FileVisitResult.CONTINUE;
    }
  }
}
