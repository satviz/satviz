package edu.kit.satviz.consumer.config;

import java.util.List;

/**
 * This enum allows for different types of sources for the embedded
 * producer to be differentiated, such as a solver file or a finished
 * proof as a DRAT-file.
 */
public enum EmbeddedModeSource {

  /**
   * Use a SAT solver as the source.
   */
  SOLVER(List.of(".so")),

  /**
   * Use a finished proof as the source.
   */
  PROOF(List.of(".drat"));

  private final List<String> fileExtensions;

  EmbeddedModeSource(List<String> fileExtensions) {
    this.fileExtensions = fileExtensions;
  }

  /**
   * Returns a list of all the file extensions that a source file of the type
   * specified by the given enum value may use.
   *
   * @return the list of file extensions
   */
  public List<String> getFileExtensions() {
    return fileExtensions;
  }

}
