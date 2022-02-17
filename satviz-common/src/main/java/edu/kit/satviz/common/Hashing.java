package edu.kit.satviz.common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.jpountz.xxhash.XXHashFactory;

public final class Hashing {

  private static final XXHashFactory HASH_FACTORY = XXHashFactory.fastestInstance();

  private Hashing() {

  }

  public static long hashContent(InputStream in) throws IOException {
    byte[] buf = new byte[8192];
    try (
        var hash = HASH_FACTORY.newStreamingHash64(0);
        var stream = new BufferedInputStream(in, 8192)
    ) {
      int read;
      while ((read = stream.read(buf)) != -1) {
        hash.update(buf, 0, read);
      }
      return hash.getValue();
    }
  }

}
