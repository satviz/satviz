package edu.kit.satviz.parsers;

/**
 * This exception is thrown, whenever the file that's currently parsed is either
 * syntactically incorrect or contains invalid data.
 */
public class ParsingException extends RuntimeException {

  /**
   * This constructor creates an instance of the <code>ParsingException</code> class.
   *
   * @param message The error message that should be displayed.
   */
  public ParsingException(String message) {
    super(message);
  }

}
