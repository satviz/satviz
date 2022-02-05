package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * This class is used to parse an <code>InputStream</code> that complies with the DIMACS CNF format.
 */
public class DimacsFile implements Iterable<ClauseUpdate>, AutoCloseable {

  private static final String INVALID_HEADER_MESSAGE =
          "The header doesn't comply with the DIMACS CNF format.";
  private static final String NO_HEADER_MESSAGE = "No header was found.";

  private final Scanner scanner;
  private final ClauseParsingIterator parsingIterator;
  private int variableAmount;
  private int clauseAmount;

  /**
   * This constructor creates an instance of the <code>DimacsFile</code> class,
   * while also parsing the header of the file.
   *
   * @param in An instance of the <code>InputStream</code> class.
   * @throws ParsingException In case no header or only an invalid header is found.
   */
  public DimacsFile(InputStream in) {
    scanner = new Scanner(in);
    parseHeader();
    parsingIterator = new DimacsParsingIterator(scanner, variableAmount, clauseAmount);
  }

  /**
   * This method parses the header of the file to get the variable and clause amount.
   */
  private void parseHeader() {
    while (scanner.hasNext("c")) {
      scanner.nextLine();
    }
    if (!scanner.hasNext()) {
      throw new ParsingException(INVALID_HEADER_MESSAGE);
    }

    String header;
    do {
      header = scanner.nextLine();
    } while (header.isBlank());

    try (Scanner headerScanner = new Scanner(header)) {
      headerScanner.useDelimiter(" ");

      if (!headerScanner.hasNext("p")) {
        throw new ParsingException(NO_HEADER_MESSAGE);
      }
      headerScanner.next();
      if (!headerScanner.hasNext("cnf")) {
        throw new ParsingException(INVALID_HEADER_MESSAGE);
      }
      headerScanner.next();
      try {
        variableAmount = headerScanner.nextInt();
      } catch (NoSuchElementException e) {
        throw new ParsingException(INVALID_HEADER_MESSAGE);
      }
      try {
        clauseAmount = headerScanner.nextInt();
      } catch (NoSuchElementException e) {
        throw new ParsingException(INVALID_HEADER_MESSAGE);
      }
      if (variableAmount < 0 || clauseAmount < 0) {
        throw new ParsingException(INVALID_HEADER_MESSAGE);
      }
      if (headerScanner.hasNext()) {
        throw new ParsingException(INVALID_HEADER_MESSAGE);
      }
    }
  }

  @Override
  public Iterator<ClauseUpdate> iterator() {
    return parsingIterator;
  }

  /**
   * This getter-method returns the variable amount, that is set in the header.
   *
   * @return The variable amount.
   */
  public int getVariableAmount() {
    return variableAmount;
  }

  /**
   * This getter-method returns the clause amount, that is set in the header.
   *
   * @return The clause amount.
   */
  public int getClauseAmount() {
    return clauseAmount;
  }

  @Override
  public void close() {
    scanner.close();
  }

}
