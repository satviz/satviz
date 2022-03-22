package edu.kit.satviz.producer;

import edu.kit.satviz.network.pub.ProducerId;
import edu.kit.satviz.producer.cli.ProducerParameters;

/**
 * A {@code ProducerMode} determines which type of {@link ClauseSource} the producer application
 * should use and simultaneously acts as a factory to create such sources.
 */
public interface ProducerMode {

  /**
   * Given a set of parameters used to configure the producer application, returns whether this
   * mode is set, i.e. whether the parameters indicate that this mode should be used.
   *
   * @param parameters the {@link ProducerParameters}
   * @return {@code true}, if this mode is set, {@code false} if not.
   */
  boolean isSet(ProducerParameters parameters);

  /**
   * Apply this mode to a set of parameters to obtain corresponding {@link ProducerModeData}.
   *
   * @param parameters the {@link ProducerParameters}
   * @return {@link ProducerModeData} containing a {@link ClauseSource} implementation and a
   *         {@link ProducerId} according to the parameters and this mode
   * @throws SourceException If the source can not be created for some reason.
   */
  ProducerModeData apply(ProducerParameters parameters) throws SourceException;

}
