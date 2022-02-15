package edu.kit.satviz.common;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link Constraint} implementation that validates that a file at a given {@link Path} exists
 * (or not).
 *
 * @see #fileExists()
 * @see #fileDoesNotExist()
 */
public class FileExistsConstraint implements Constraint<Path> {

  private final boolean mustExist;

  /**
   * Constructor for this constraint type.
   *
   * @param mustExist If set to {@code true}, the constraint will fail if a file
   *                  <em>does not exist</em>, if set to {@code false}, the opposite.
   * @see #fileExists()
   * @see #fileDoesNotExist()
   */
  public FileExistsConstraint(boolean mustExist) {
    this.mustExist = mustExist;
  }

  @Override
  public void validate(Path obj) throws ConstraintValidationException {
    if (Files.exists(obj) ^ mustExist) {
      fail(obj + (mustExist ? " does not exist" : " already exists"));
    }
  }

  /**
   * Produces a version of this constraint that checks whether an input file <em>exists</em>.
   *
   * @return A constraint that fails if a given file does not exist.
   */
  public static FileExistsConstraint fileExists() {
    return new FileExistsConstraint(true);
  }

  /**
   * Produces a version of this constraint that checks whether an input file
   * <em>does not exist</em>.
   *
   * @return A constraint that fails if a given file exists.
   */
  public static FileExistsConstraint fileDoesNotExist() {
    return new FileExistsConstraint(false);
  }
}
