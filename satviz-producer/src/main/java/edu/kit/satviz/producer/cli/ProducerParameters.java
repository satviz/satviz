package edu.kit.satviz.producer.cli;

import net.sourceforge.argparse4j.annotation.Arg;

import java.nio.file.Path;

public final class ProducerParameters {

  @Arg(dest = "instance")
  private Path instanceFile;

  @Arg(dest = "solver")
  private Path solverFile;

  @Arg(dest = "proof")
  private Path proofFile;

  @Arg
  private int port;

  @Arg
  private String host;

  @Arg
  private boolean noWait;

  public Path getInstanceFile() {
    return instanceFile;
  }

  public Path getSolverFile() {
    return solverFile;
  }

  public Path getProofFile() {
    return proofFile;
  }

  public int getPort() {
    return port;
  }

  public String getHost() {
    return host;
  }

  public boolean isNoWait() {
    return noWait;
  }

  public void setInstanceFile(Path instanceFile) {
    this.instanceFile = instanceFile;
  }

  public void setSolverFile(Path solverFile) {
    this.solverFile = solverFile;
  }

  public void setProofFile(Path proofFile) {
    this.proofFile = proofFile;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setNoWait(boolean noWait) {
    this.noWait = noWait;
  }
}
