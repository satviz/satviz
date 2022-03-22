package edu.kit.satviz.consumer.processing;

import static edu.kit.satviz.consumer.processing.Constants.UPDATES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.graph.WeightUpdate;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RingInteractionGraphTest {

  private VariableInteractionGraph vig;

  @BeforeEach
  void setUp() {
    vig = new RingInteractionGraph(WeightFactor.CONSTANT);
  }

  @Test
  void test_process() {
    var result = vig.process(Arrays.copyOfRange(UPDATES, 0, 1), null, IdentityMapping.INSTANCE);
    var expected = new WeightUpdate();
    expected.add(0, 2, 1);
    expected.add(2, 4, 1);
    expected.add(4, 5, 1);
    expected.add(0, 5, 1);
    assertEquals(expected, result);
  }

  @Test
  void test_process_ignored() {
    var result = vig.process(new ClauseUpdate[] {ClauseUpdate.of(ClauseUpdate.Type.ADD, 3)}, null, IdentityMapping.INSTANCE);
    var expected = new WeightUpdate();
    assertEquals(expected, result);
  }
}
