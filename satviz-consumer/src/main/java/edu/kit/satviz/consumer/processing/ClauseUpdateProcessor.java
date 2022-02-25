package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.GraphUpdate;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.SerializationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This interface allows for instances of the {@code ClauseUpdate} class to be
 * processed into {@code GraphUpdate}s.
 */
public interface ClauseUpdateProcessor {

  /**
   * This method processes an array of the {@code ClauseUpdate}s into {@code GraphUpdate}s.
   *
   * @param clauseUpdates An array of {@code ClauseUpdate}s.
   * @param graph An instance of the {@code Graph} class.
   * @return An instance of the {@code GraphUpdate} class.
   */
  GraphUpdate process(ClauseUpdate[] clauseUpdates, Graph graph);

  /**
   * This method serializes the internal state of the processor.<br>
   * <i>Note: Not every processor needs internal state.</i>
   *
   * @param out The {@code OutputStream} this processor's state is serialized to.
   */
  default void serialize(OutputStream out) throws IOException {

  }

  /**
   * This method deserializes the internal state of the processor from a given {@code InputStream}.
   *
   * @param in The {@code InputStream} this processor's state is deserialized from.
   */
  default void deserialize(InputStream in) throws IOException, SerializationException {

  }

  /**
   * This method resets this processor to its original state.
   */
  default void reset() {

  }

}
