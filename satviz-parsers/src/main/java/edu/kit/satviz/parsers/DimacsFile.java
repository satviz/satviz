package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class DimacsFile implements Iterable<ClauseUpdate>, AutoCloseable {

  private static final String INVALID_FILE_MESSAGE = "File is invalid.";

  private final Scanner scanner;
  private final ClauseParsingIterator parsingIterator;
  private int variableAmount;
  private int clauseAmount;

  public DimacsFile(InputStream in) {
    scanner = new Scanner(in);
    parseHeader();
    parsingIterator = new DimacsParsingIterator(scanner);
  }

  private void parseHeader() {
    while (scanner.hasNext("c")) {
      scanner.nextLine();
    }
    if (!scanner.hasNext()) {
      throw new ParsingException(INVALID_FILE_MESSAGE);
    }

    String header;
    do {
      header = scanner.nextLine();
    } while (header.isBlank());

    try (Scanner headerScanner = new Scanner(header)) {
      headerScanner.useDelimiter(" ");

      if (!headerScanner.hasNext("p")) {
        throw new ParsingException(INVALID_FILE_MESSAGE);
      }
      headerScanner.next();
      if (!headerScanner.hasNext("cnf")) {
        throw new ParsingException(INVALID_FILE_MESSAGE);
      }
      headerScanner.next();
      try {
        variableAmount = headerScanner.nextInt();
      } catch (NoSuchElementException e) {
        throw new ParsingException(INVALID_FILE_MESSAGE);
      }
      try {
        clauseAmount = headerScanner.nextInt();
      } catch (NoSuchElementException e) {
        throw new ParsingException(INVALID_FILE_MESSAGE);
      }
      if (headerScanner.hasNext()) {
        throw new ParsingException(INVALID_FILE_MESSAGE);
      }
    }
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
  public void close() {
    scanner.close();
  }

}
