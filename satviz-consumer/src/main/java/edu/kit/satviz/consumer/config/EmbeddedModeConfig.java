package edu.kit.satviz.consumer.config;

import java.nio.file.Path;
import java.util.Objects;

public class EmbeddedModeConfig extends ConsumerModeConfig {

  private EmbeddedModeSource source;
  private Path sourcePath;

  public void setSource(EmbeddedModeSource source) {
    this.source = source;
  }

  public void setSourcePath(Path sourcePath) {
    this.sourcePath = sourcePath;
  }

  public Path getSourcePath() {
    return sourcePath;
  }

  public EmbeddedModeSource getSource() {
    return source;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }
    EmbeddedModeConfig that = (EmbeddedModeConfig) o;
    return source == that.source && Objects.equals(sourcePath, that.sourcePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), source, sourcePath);
  }
}
