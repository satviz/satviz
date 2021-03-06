package edu.kit.satviz.producer.mode;

import edu.kit.satviz.common.Compression;
import edu.kit.satviz.network.pub.ProofId;
import edu.kit.satviz.parsers.DratFile;
import edu.kit.satviz.producer.ProducerMode;
import edu.kit.satviz.producer.ProducerModeData;
import edu.kit.satviz.producer.SourceException;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.source.ProofSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * A mode for when the producer should get its clauses from a DRAT proof.
 */
public class ProofMode implements ProducerMode {
  @Override
  public boolean isSet(ProducerParameters parameters) {
    return parameters.getProofFile() != null;
  }

  @Override
  public ProducerModeData apply(ProducerParameters parameters) throws SourceException {
    try {
      InputStream proofStream = Compression.openPossiblyCompressed(parameters.getProofFile());
      DratFile drat = new DratFile(proofStream);
      return new ProducerModeData(
          new ProofSource(drat),
          new ProofId()
      );
    } catch (IOException e) {
      throw new SourceException("Could not create source - I/O error", e);
    }
  }
}
