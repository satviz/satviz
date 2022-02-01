package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public abstract class ClauseParsingIterator implements Iterator<ClauseUpdate> {

  private static final String NO_CLAUSES_LEFT_MESSAGE = "No clause updates left.";
  private static final String INVALID_FILE_MESSAGE = "File is invalid.";

  final Scanner scanner;
  private ClauseUpdate nextUpdate;

  private boolean isDone = false;
  private boolean isInvalidFile = false;

  protected ClauseParsingIterator(Scanner scanner) {
    this.scanner = scanner;
  }

  private ClauseUpdate getNextUpdate() {
    // Skip all comment-lines
    while (scanner.hasNext("c")) {
      scanner.nextLine();
    }
    // If no lines are left, no more clause can be returned
    if (!scanner.hasNext()) {
      isDone = true;
      throw new NoSuchElementException(NO_CLAUSES_LEFT_MESSAGE);
    }

    final ClauseUpdate.Type type = readType();

    // Clause parsing starts here
    ArrayList<Integer> list = new ArrayList<>();
    while (!scanner.hasNext("0")) {
      if (scanner.hasNextInt()) {
        list.add(scanner.nextInt());
      } else {
        isInvalidFile = true;
        throw new ParsingException(INVALID_FILE_MESSAGE);
      }
    }
    // In case the file ends before the clause is finished with a 0
    if (!scanner.hasNext()) {
      isInvalidFile = true;
      throw new ParsingException(INVALID_FILE_MESSAGE);
    }
    scanner.next();

    Clause clause = new Clause(list.stream().mapToInt(i -> i).toArray());
    return new ClauseUpdate(clause, type);
  }

  @Override
  public boolean hasNext() {
    if (isDone || isInvalidFile) {
      return false;
    }
    if (nextUpdate == null) {
      try {
        nextUpdate = getNextUpdate();
      } catch (NoSuchElementException e) {
        isDone = true;
        nextUpdate = null;
        return false;
      } catch (ParsingException e) {
        isInvalidFile = true;
        nextUpdate = null;
        throw e;
      }
    }
    return true;
  }

  @Override
  public ClauseUpdate next() {
    if (isDone) {
      throw new NoSuchElementException(NO_CLAUSES_LEFT_MESSAGE);
    } else if (isInvalidFile) {
      throw new ParsingException(INVALID_FILE_MESSAGE);
    }
    ClauseUpdate next = (nextUpdate != null) ? nextUpdate : getNextUpdate();
    nextUpdate = null;
    return next;
  }

  protected abstract ClauseUpdate.Type readType();

}
