package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;

import java.util.Scanner;

public class DratParsingIterator extends ClauseParsingIterator {

  protected DratParsingIterator(Scanner scanner) {
    super(scanner);
  }

  @Override
  protected ClauseUpdate.Type readType(String updateString) {
    return null;
  }

}
