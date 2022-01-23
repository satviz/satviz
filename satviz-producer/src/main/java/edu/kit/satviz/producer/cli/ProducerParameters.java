package edu.kit.satviz.producer.cli;

import java.nio.file.Path;

public final class ProducerParameters {

  private Path instanceFile;
  private Path solverFile;
  private Path proofFile;
  private int port;
  private String host;
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
