package edu.kit.satviz.producer;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class ResourceHelper {

  public static List<ClauseUpdate> PROOF_UPDATES = List.of(
      new ClauseUpdate(new Clause(new int[] {1, 2, 3}), ClauseUpdate.Type.ADD),
      new ClauseUpdate(new Clause(new int[] {3, -4}), ClauseUpdate.Type.REMOVE),
      new ClauseUpdate(new Clause(new int[] {2, -1}), ClauseUpdate.Type.ADD)
  );

  private static Path tempDir;

  public static void createTempDir() throws IOException {
    tempDir = Files.createTempDirectory("producer-test");
  }

  public static void deleteTempDir() throws IOException {
    Files.walkFileTree(tempDir, new DeletingVisitor());
    tempDir = null;
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
