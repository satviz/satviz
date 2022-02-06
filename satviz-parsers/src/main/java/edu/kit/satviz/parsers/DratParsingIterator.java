package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Scanner;

/**
 * This class is used to validate and read the clause types from a file in the DRAT format.
 */
public class DratParsingIterator extends ClauseParsingIterator {

  /**
   * his constructor creates an instance of the <code>DratParsingIterator</code> class.
   *
   * @param scanner The scanner, that scans the DRAT file.
   */
  protected DratParsingIterator(Scanner scanner) {
    super(scanner);
  }

  @Override
  protected ClauseUpdate.Type readType() {
    if (scanner.hasNext("d")) {
      scanner.next();
      return ClauseUpdate.Type.REMOVE;
    } else {
      return ClauseUpdate.Type.ADD;
    }
  }

  @Override
  protected boolean isValidVariable(int variable) {
    return true;
  }

  @Override
  protected boolean isValidClauseUpdate(ClauseUpdate clauseUpdate) {
    return true;
  }

  @Override
  protected boolean isFinalClauseUpdate(ClauseUpdate clauseUpdate) {
    return clauseUpdate.clause().literals().length == 0;
  }

  @Override
  protected boolean isPrematureEndOfFile() {
    return true;
  }

}
