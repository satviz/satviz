package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Scanner;

public class DimacsParsingIterator extends ClauseParsingIterator {

  private final int variableAmount;
  private final int clauseAmount;

  private int counter = 0;

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
  protected boolean isValidClauseUpdate(ClauseUpdate clause) {
    return ++counter <= clauseAmount;
  }

  @Override
  protected boolean isFinalClauseUpdate(ClauseUpdate clauseUpdate) {
    return false;
  }

  @Override
  protected boolean isPrematureEndOfFile() {
    return false;
  }

}
