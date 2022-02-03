package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public abstract class ClauseParsingIterator implements Iterator<ClauseUpdate> {

  private static final String COMMENT_LINE_TOKEN = "c";
  private static final String CLAUSE_END_TOKEN = "0";

  private static final String NO_CLAUSES_LEFT_MESSAGE = "No clause updates left.";
  private static final String UNEXPECTED_VARIABLE_MESSAGE = "The variable %s is illegal.";
  private static final String UNEXPECTED_CLAUSE_MESSAGE = "There is an unexpected clause.";
  private static final String UNEXPECTED_END_MESSAGE = "Unexpected end of file.";
  private String unexpectedMessage;

  final Scanner scanner;
  private ClauseUpdate nextUpdate;

  private boolean isDone = false;
  private boolean isInvalidFile = false;

  protected ClauseParsingIterator(Scanner scanner) {
    this.scanner = scanner;
  }

  @Override
  public boolean hasNext() {
    if (nextUpdate != null) {
      return true;
    }
    if (isDone || isInvalidFile) {
      return false;
    }
    try {
      nextUpdate = getNextUpdate();
    } catch (NoSuchElementException | ParsingException e) {
      nextUpdate = null;
      return false;
    }
    return true;
  }

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
   * This method parses the next clause update.
   *
   * @return An instance of the <code>ClauseUpdate</code> class.
   * @throws ParsingException In case the given file is invalid.
   *         <i>Validation is partly implemented in the subclasses.</i>
   * @throws NoSuchElementException In case there are no clause updates left to read in the file.
   */
  private ClauseUpdate getNextUpdate() {
    skipCommentLines();
    final ClauseUpdate.Type type = readType();
    final ClauseUpdate clauseUpdate = new ClauseUpdate(parseClause(), type);

    if (!isValidClauseUpdate(clauseUpdate)) {
      throwParsingException(UNEXPECTED_CLAUSE_MESSAGE);
    } else if (isFinalClauseUpdate(clauseUpdate)) {
      isDone = true;
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
        throwParsingException(UNEXPECTED_VARIABLE_MESSAGE, scanner.next());
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

  private void throwParsingException(String message) {
    isInvalidFile = true;
    unexpectedMessage = message;
    throw new ParsingException(message);
  }

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
