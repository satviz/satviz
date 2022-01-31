package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

public class DimacsFile implements Iterable<ClauseUpdate>, AutoCloseable {

  private final InputStream in;
  private final ClauseParsingIterator parsingIterator;
  private int variableAmount;
  private int clauseAmount;

  public DimacsFile(InputStream in) {
    this.in = in;
    Scanner scanner = new Scanner(in);
    scanner.useDelimiter("\n");
    parseHeader(scanner);
    parsingIterator = new DimacsParsingIterator(scanner);
  }

  private void parseHeader(Scanner scanner) {
    String header;
    do {
      header = scanner.next();
    } while (header.matches("p cnf (([1-9][0-9]*)|0) (([1-9][0-9]*)|0)"));
    String[] numberStrings = header.replaceAll("[\\D]", " ").split(" ");
    numberStrings = Arrays.stream(numberStrings)
            .filter(value ->
                    value != null && value.length() > 0
            )
            .toArray(String[]::new);
    variableAmount = Integer.parseInt(numberStrings[0]);
    clauseAmount = Integer.parseInt(numberStrings[1]);
  }

  @Override
  public Iterator<ClauseUpdate> iterator() {
    return parsingIterator;
  }

  public int getVariableAmount() {
    return variableAmount;
  }

  public int getClauseAmount() {
    return clauseAmount;
  }

  @Override
  public void close() throws IOException {
    in.close();
  }

}
