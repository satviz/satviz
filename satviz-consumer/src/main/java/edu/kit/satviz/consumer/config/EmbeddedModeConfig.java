package edu.kit.satviz.consumer.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.nio.file.Path;
import java.util.Objects;

/**
 * This subclass of the <code>ConsumerModeConfig</code> class is used, when one decides
 * to use the consumer with an embedded producer.
 */
@JsonTypeName("EMBEDDED")
public class EmbeddedModeConfig extends ConsumerModeConfig {

  public static final EmbeddedModeSource DEFAULT_EMBEDDED_MODE_SOURCE = EmbeddedModeSource.SOLVER;

  private EmbeddedModeSource source;
  private Path sourcePath;

  /**
   * This constructor creates an instance of the EmbeddedModeConfig class.
   */
  public EmbeddedModeConfig() {
    super.setMode(ConsumerMode.EMBEDDED);
  }

  /**
   * This method sets the source type for the embedded producer.
   * This is specified through an instance of the <code>EmbeddedModeSource</code> enum.
   *
   * @param source An instance of the <code>EmbeddedModeSource</code> enum.
   */
  public void setSource(EmbeddedModeSource source) {
    this.source = source;
  }

  /**
   * This method sets the path in which the source for the clauses is located.
   *
   * @param sourcePath An instance of the Path class.
   */
  public void setSourcePath(Path sourcePath) {
    this.sourcePath = sourcePath;
  }

  /**
   * This getter-method returns the source type for the embedded producer
   * as an instance of the <code>EmbeddedModeSource</code> enum.
   *
   * @return An instance of the <code>EmbeddedModeSource</code> enum.
   */
  public Path getSourcePath() {
    return sourcePath;
  }

  /**
   * This getter-method returns the path in which the source for the clauses
   * is located.
   *
   * @return The path in which the source for the clauses is located.
   */
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
