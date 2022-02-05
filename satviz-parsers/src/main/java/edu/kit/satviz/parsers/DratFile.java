package edu.kit.satviz.parsers;

import edu.kit.satviz.sat.ClauseUpdate;
import java.io.InputStream;
import java.util.Iterator;

/**
 * This class is used to parse an <code>InputStream</code> that complies with the DRAT format.
 */
public class DratFile extends ClauseFile {

  /**
   * This constructor creates an instance of the <code>DratFile</code> class.
   *
   * @param in An instance of the <code>InputStream</code> class.
   */
  public DratFile(InputStream in) {
    super(in);
  }

  @Override
  protected void parseHeader() {
    // This method is empty, because in DRAT Files there is no header.
  }

  @Override
  public Iterator<ClauseUpdate> iterator() {
    return new DratParsingIterator(scanner);
  }

}
