package edu.kit.satviz.consumer.config;

import java.nio.file.Path;

public class EmbeddedModeConfig extends ConsumerModeConfig {

  private EmbeddedModeSource source;
  private Path sourcePath;

  public Path getSourcePath() {
    return sourcePath;
  }

  public EmbeddedModeSource getSource() {
    return source;
  }
}
