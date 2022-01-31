package edu.kit.satviz.serial;

import java.io.InputStream;

public class ByteArrayInputStream extends InputStream {
  private final byte[] b;
  private int readpos = 0;

  public ByteArrayInputStream(byte[] b) {
    this.b = b;
  }

  @Override
  public int read() {
    if (readpos == b.length) {
      return -1;
    }
    return b[readpos++] & 0xff; // weird java sign extension
  }

  public void reset() {
    readpos = 0;
  }
}
