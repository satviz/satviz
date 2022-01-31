package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public abstract class ClauseParsingIterator implements Iterator<ClauseUpdate> {

  private static final String NO_CLAUSES_LEFT_MESSAGE = "No clause updates left.";
  private static final String INVALID_FILE_MESSAGE = "File is invalid.";

  private final Scanner scanner;
  private ClauseUpdate nextUpdate;
  private String buffer = "";

  private boolean isDone = false;
  private boolean isInvalidFile = false;

  protected ClauseParsingIterator(Scanner scanner) {
    this.scanner = scanner;
  }

  private ClauseUpdate getNextUpdate() {
    String updateString;
    if (buffer.equals("")) {
      do {
        if (scanner.hasNext()) {
          updateString = scanner.next();
        } else {
          isDone = true;
          throw new NoSuchElementException(NO_CLAUSES_LEFT_MESSAGE);
        }
      } while (updateString.startsWith("c "));
    } else {
      updateString = buffer;
    }

    final ClauseUpdate.Type type = readType(updateString);

    int endIndex;
    StringBuilder builder = new StringBuilder(updateString);
    while ((endIndex = updateString.indexOf(" 0")) == -1
                    && (endIndex = updateString.indexOf("\t0")) == -1) {
      if (scanner.hasNext()) {
        builder.append(" ");
        builder.append(scanner.next());
      } else {
        isInvalidFile = true;
        throw new ParsingException(INVALID_FILE_MESSAGE);
      }
    }
    updateString = builder.toString();

    buffer = updateString.substring(endIndex + 1).trim();
    updateString = updateString.substring(0, endIndex + 1).trim();
    Clause clause = new Clause(
            Arrays.stream(
                    updateString.split(" *") // dieser Split könnte immernoch falsch sein!
            ).mapToInt(Integer::parseInt).toArray()
    );
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
        return false;
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

  protected abstract ClauseUpdate.Type readType(String updateString);

}
