package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Scanner;

/**
 * This class is used to validate a file in the CNF format.
 */
public class DimacsParsingIterator extends ClauseParsingIterator {

  private final int variableAmount;
  private final int clauseAmount;

  private int counter = 0;

  /**
   * This constructor creates an instance of the <code>DimacsParsingIterator</code> class.
   *
   * @param scanner The scanner, that scans the CNF file.
   * @param variableAmount The variable amount, that is read in the header of the file.
   * @param clauseAmount The clause amount, that is read in the header of the file.
   */
  protected DimacsParsingIterator(Scanner scanner, int variableAmount, int clauseAmount) {
    super(scanner);
    this.variableAmount = variableAmount;
    this.clauseAmount = clauseAmount;
  }

  @Override
  protected ClauseUpdate.Type readType() {
    return ClauseUpdate.Type.ADD;
  }

  @Override
  protected boolean isValidVariable(int variable) {
    return Math.abs(variable) <= variableAmount;
  }

  @Override
  protected boolean isValidClauseUpdate(ClauseUpdate clauseUpdate) {
    return ++counter <= clauseAmount;
  }

  @Override
  protected boolean isFinalClauseUpdate(ClauseUpdate clauseUpdate) {
    return false;
  }

  @Override
  protected boolean isPrematureEndOfFile() {
    return counter < clauseAmount;
  }

}
