package edu.kit.satviz.consumer.cli;

import edu.kit.satviz.consumer.config.ExternalModeConfig;
import net.sourceforge.argparse4j.annotation.Arg;

import java.nio.file.Path;

/**
 *
 */
public final class ModeConfigParameters {

  // embedded mode arguments
  @Arg(dest = "solver")
  private Path solverFile;

  @Arg(dest = "proof")
  private Path proofFile;

  // external mode arguments
  @Arg
  private int port = ExternalModeConfig.DEFAULT_PORT_NUMBER;


  /**
   *
   * @param solverFile
   */
  public void setSolverFile(Path solverFile) {
    this.solverFile = solverFile;
  }

  /**
   *
   * @param proofFile
   */
  public void setProofFile(Path proofFile) {
    this.proofFile = proofFile;
  }

  /**
   *
   * @param port
   */
  public void setPort(int port) {
    this.port = port;
  }


  /**
   *
   * @return
   */
  public Path getSolverFile() {
    return solverFile;
  }

  /**
   *
   * @return
   */
  public Path getProofFile() {
    return proofFile;
  }

  /**
   *
   * @return
   */
  public int getPort() {
    return port;
  }

}
