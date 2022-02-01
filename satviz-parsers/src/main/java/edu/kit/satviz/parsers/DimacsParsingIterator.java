package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Scanner;

public class DimacsParsingIterator extends ClauseParsingIterator {

  protected DimacsParsingIterator(Scanner scanner) {
    super(scanner);
  }

  @Override
  protected ClauseUpdate.Type readType() {
    return ClauseUpdate.Type.ADD;
  }

}
