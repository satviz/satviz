package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Scanner;

public class DratParsingIterator extends ClauseParsingIterator {

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

}
