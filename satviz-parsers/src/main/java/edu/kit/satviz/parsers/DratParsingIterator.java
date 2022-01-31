package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Scanner;

public class DratParsingIterator extends ClauseParsingIterator {

  protected DratParsingIterator(Scanner scanner) {
    super(scanner);
  }

  @Override
  protected ClauseUpdate.Type readType(String updateString) {
    if (updateString.matches("(-?[1-9][0-9]*[ \t]+)*0")) {                // might overflow?
      return ClauseUpdate.Type.ADD;
    } else if (updateString.matches("d[ \t]+(-?[1-9][0-9]*[ \t]+)*0")) {  // might overflow?
      return ClauseUpdate.Type.REMOVE;
    } else {
      throw new ParsingException("Invalid clause.");
    }
  }

}
