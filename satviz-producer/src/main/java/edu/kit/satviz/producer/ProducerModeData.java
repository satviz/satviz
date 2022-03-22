package edu.kit.satviz.producer;

import edu.kit.satviz.network.pub.ProducerId;

/**
 * The data a {@link ProducerMode} creates upon initialisation with a set of parameters.
 *
 * @param source The {@link ClauseSource} that will produce the clause updates.
 * @param id The {@link ProducerId} to send over the network.
 */
public record ProducerModeData(ClauseSource source, ProducerId id) {
}
