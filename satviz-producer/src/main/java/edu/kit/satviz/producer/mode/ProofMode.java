package edu.kit.satviz.producer.mode;

import edu.kit.satviz.parsers.DratFile;
import edu.kit.satviz.producer.ClauseSource;
import edu.kit.satviz.producer.ProducerMode;
import edu.kit.satviz.producer.SourceException;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.source.ProofSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ProofMode implements ProducerMode {
  @Override
  public boolean isSet(ProducerParameters parameters) {
    return parameters.getProofFile() != null;
  }

  @Override
  public ClauseSource createSource(ProducerParameters parameters) throws SourceException {
    try {
      InputStream proofStream = Files.newInputStream(parameters.getProofFile());
      DratFile drat = new DratFile(proofStream);
      return new ProofSource(drat);
    } catch (IOException e) {
      throw new SourceException("Could not create source - I/O error", e);
    }
  }
}
