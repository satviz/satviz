package edu.kit.satviz.serial;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayOutputStream extends OutputStream {
  private final byte[] b;
  private int writepos = 0;

  public ByteArrayOutputStream(byte[] b) {
    this.b = b;
  }

  @Override
  public void write(int i) throws IOException {
    if (writepos == b.length) {
      throw new IOException("stream finished");
    }
    b[writepos++] = (byte) i;
  }

  public void reset() {
    writepos = 0;
  }
}