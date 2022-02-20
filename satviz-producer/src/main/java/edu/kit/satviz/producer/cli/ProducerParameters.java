package edu.kit.satviz.producer.cli;

import java.nio.file.Path;
import java.util.Objects;
import net.sourceforge.argparse4j.annotation.Arg;

/**
 * A data class representing the parameters which can be used to configure the producer application.
 */
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

  @Arg(dest = "no_wait")
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProducerParameters that = (ProducerParameters) o;
    return port == that.port
        && noWait == that.noWait
        && Objects.equals(instanceFile, that.instanceFile)
        && Objects.equals(solverFile, that.solverFile)
        && Objects.equals(proofFile, that.proofFile)
        && Objects.equals(host, that.host);
  }

  @Override
  public int hashCode() {
    return Objects.hash(instanceFile, solverFile, proofFile, port, host, noWait);
  }

  @Override
  public String toString() {
    return "ProducerParameters{"
        + "instanceFile=" + instanceFile
        + ", solverFile=" + solverFile
        + ", proofFile=" + proofFile
        + ", port=" + port
        + ", host='" + host + '\''
        + ", noWait=" + noWait
        + '}';
  }
}
