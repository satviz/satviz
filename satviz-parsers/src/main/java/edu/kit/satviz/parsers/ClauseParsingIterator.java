package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * This abstract class is used to parse CNF and DRAT files (and maybe other filetypes in the future)
 * into instances of the <code>ClauseUpdate</code> class by implementing the
 * <code>Iterator</code> interface.
 */
public abstract class ClauseParsingIterator implements Iterator<ClauseUpdate> {

  private static final String COMMENT_LINE_TOKEN = "c";
  private static final String CLAUSE_END_TOKEN = "0";

  private static final String NO_CLAUSES_LEFT_MESSAGE = "No clause updates left.";
  private static final String UNEXPECTED_CHAR_MESSAGE = "\"%s\" contains illegal characters.";
  private static final String UNEXPECTED_VARIABLE_MESSAGE = "The variable \"%s\" is invalid.";
  private static final String UNEXPECTED_CLAUSE_MESSAGE = "There is an unexpected clause.";
  private static final String UNEXPECTED_END_MESSAGE = "Unexpected end of file.";
  private String unexpectedMessage;

  protected final Scanner scanner;
  private ClauseUpdate nextUpdate;

  private boolean isDone = false;
  private boolean isInvalidFile = false;

  private boolean isFinalUpdateIncluded = false;

  /**
   * This constructor creates an instance of the <code>ClauseParsingIterator</code> class.
   *
   * @param scanner The scanner, which scans through the file that should be parsed.
   */
  protected ClauseParsingIterator(Scanner scanner) {
    this.scanner = scanner;
  }

  @Override
  public boolean hasNext() {
    if (isInvalidFile) {
      throw new ParsingException(unexpectedMessage);
    } else if (nextUpdate != null) {
      return true;
    } else if (isDone) {
      return false;
    }
    try {
      nextUpdate = getNextUpdate();
    } catch (NoSuchElementException e) {
      nextUpdate = null;
      return false;
    }
    return true;
  }

  /**
   * This method parses the next clause update.
   *
   * @return An instance of the <code>ClauseUpdate</code> class.
   * @throws ParsingException In case the given file is invalid.
   *         <i>Validation is partly implemented in the subclasses.</i>
   * @throws NoSuchElementException In case there are no clause updates left to read in the file.
   */
  @Override
  public ClauseUpdate next() {
    if (nextUpdate != null) {
      ClauseUpdate next = nextUpdate;
      nextUpdate = null;
      return next;
    }
    if (isDone) {
      throw new NoSuchElementException(NO_CLAUSES_LEFT_MESSAGE);
    } else if (isInvalidFile) {
      throw new ParsingException(unexpectedMessage);
    }
    return getNextUpdate();
  }

  /**
   * This method sets the final clause update to be included, if <i>true</i> is entered,
   * and excluded, if <i>false</i> is entered. By default, it's <b>excluded</b>.<br>
   * <br>
   * The final update can be specified by the subclass with the <code>isFinalClauseUpdate()</code>
   * method.
   *
   * @param isIncluded Set to <i>true</i>, if the final clause update should be included,
   *                   to <i>false</i>, if not.
   */
  public void setFinalClauseUpdateIncluded(boolean isIncluded) {
    isFinalUpdateIncluded = isIncluded;
  }

  /**
   * This getter-method returns, whether the final clause update is included.
   * By default, it is not.
   *
   * @return <i>true</i>, if the final clause update is included,<br>
   *         <i>false</i>, if not.
   */
  public boolean isFinalUpdateIncluded() {
    return isFinalUpdateIncluded;
  }

  private ClauseUpdate getNextUpdate() {
    skipCommentLines();
    final ClauseUpdate.Type type = readType();
    final ClauseUpdate clauseUpdate = new ClauseUpdate(parseClause(), type);

    if (!isValidClauseUpdate(clauseUpdate)) {
      throwParsingException(UNEXPECTED_CLAUSE_MESSAGE);
    } else if (isFinalClauseUpdate(clauseUpdate)) {
      isDone = true;
      if (!isFinalUpdateIncluded) {
        throw new NoSuchElementException(NO_CLAUSES_LEFT_MESSAGE);
      }
    }
    return clauseUpdate;
  }

  /**
   * This method skips all commented lines.
   */
  private void skipCommentLines() {
    while (scanner.hasNext(COMMENT_LINE_TOKEN)) {
      scanner.nextLine();
    }
    // If no lines are left, no more clauses can be returned
    if (!scanner.hasNext()) {
      if (!isPrematureEndOfFile()) {
        isDone = true;
        throw new NoSuchElementException(NO_CLAUSES_LEFT_MESSAGE);
      } else {
        throwParsingException(UNEXPECTED_END_MESSAGE);
      }
    }
  }

  /**
   * This method parses a clause.
   *
   * @return An instance of the <code>Clause</code> record.
   */
  private Clause parseClause() {
    int variable;
    ArrayList<Integer> list = new ArrayList<>();
    while (!scanner.hasNext(CLAUSE_END_TOKEN)) {
      // in case the file ends before the clause is finished with a 0.
      if (!scanner.hasNext()) {
        throwParsingException(UNEXPECTED_END_MESSAGE);
      }
      // in case the next variable is not an integer.
      if (!scanner.hasNextInt()) {
        throwParsingException(UNEXPECTED_CHAR_MESSAGE, scanner.next());
      }
      variable = scanner.nextInt();
      // in case the next variable is invalid.
      if (!isValidVariable(variable)) {
        throwParsingException(UNEXPECTED_VARIABLE_MESSAGE, String.valueOf(variable));
      }
      list.add(variable);
    }
    scanner.next();

    return new Clause(list.stream().mapToInt(i -> i).toArray());
  }

  /**
   * This method throws a <code>ParsingException</code> with the entered message,
   * while setting <code>isInvalidFile</code> to <code>true</code>.
   *
   * @param message The error message that should be displayed for the
   *                <code>ParsingException</code>.
   */
  private void throwParsingException(String message) {
    isInvalidFile = true;
    unexpectedMessage = message;
    throw new ParsingException(message);
  }

  /**
   * This method throws a <code>ParsingException</code> with the entered message,
   * while setting <code>isInvalidFile</code> to <code>true</code>.<br>
   * To make the message dynamic <code>String.format(message, unexpectedToken)</code> is used.
   *
   * @param message The error message that should be displayed for the
   *                <code>ParsingException</code>.
   */
  private void throwParsingException(String message, String unexpectedToken) {
    isInvalidFile = true;
    unexpectedMessage = String.format(message, unexpectedToken);
    throw new ParsingException(unexpectedMessage);
  }



  /**
   * This method determines the specific type of clause update.<br>
   * <i>It may use the scanner to read the identifier-tokens of the type.</i>
   *
   * @return An instance of the <code>ClauseUpdate.Type</code> enum.
   */
  protected abstract ClauseUpdate.Type readType();

  /**
   * This method checks the validity of an entered variable.
   *
   * @param variable An integer-representation of a variable.
   * @return <i>true</i>, if the variable is valid,<br>
   *         <i>false</i>, if not.
   */
  protected abstract boolean isValidVariable(int variable);

  /**
   * This method checks the validity of a clause.
   *
   * @param clauseUpdate An instance of the <code>ClauseUpdate</code> class.
   * @return <i>true</i>, if the clause is valid,<br>
   *         <i>false</i>, if not.
   */
  protected abstract boolean isValidClauseUpdate(ClauseUpdate clauseUpdate);

  /**
   * This method checks, whether the entered clause is the last clause, that should be read.
   *
   * @param clauseUpdate An instance of the <code>ClauseUpdate</code> class.
   * @return <i>true</i>, if the clause is the last clause, that should be read,<br>
   *         <i>false</i>, if not.
   */
  protected abstract boolean isFinalClauseUpdate(ClauseUpdate clauseUpdate);

  /**
   * This method checks, whether the file has ended prematurely.
   *
   * @return <i>true</i>, if the file has ended prematurely,<br>
   *         <i>false</i>, if not.
   */
  protected abstract boolean isPrematureEndOfFile();

}
