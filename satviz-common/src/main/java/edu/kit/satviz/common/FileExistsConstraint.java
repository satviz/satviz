package edu.kit.satviz.common;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileExistsConstraint implements Constraint<Path> {

  private final boolean mustExist;

  public FileExistsConstraint(boolean mustExist) {
    this.mustExist = mustExist;
  }

  @Override
  public void validate(Path obj) throws ConstraintValidationException {
    if (Files.exists(obj) ^ mustExist) {
      fail(obj + (mustExist ? " does not exist" : " already exists"));
    }
  }
}
