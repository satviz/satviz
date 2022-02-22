package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.WeightFactor;
import edu.kit.satviz.consumer.graph.WeightUpdate;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The following tests check the functionality of the <code>VariableInteractionGraph</code> class.
 */
class VariableInteractionGraphTest {

  private static final WeightFactor INITIAL_FACTOR = WeightFactor.CONSTANT;

  /**
   * Clauses, that could be interpreted as an entire DRAT proof:
   * <pre>
   *         -1 0
   * d -1 -2  3 0
   * d -1 -3 -4 0
   * d -1  2  4 0
   *          2 0</pre>
   */
  private static final ClauseUpdate[] clauseUpdates = new ClauseUpdate[] {
          new ClauseUpdate(new Clause(new int[]{-1}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{-1, -2, 3}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{-1, -3, -4}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{-1, 2, 4}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{2}), ClauseUpdate.Type.ADD)
  };

  private static final Map<WeightFactor, WeightUpdate> WEIGHT_UPDATES = new HashMap<>();

  private VariableInteractionGraph vig;

  /**
   * This method initializes the correct weight-updates for them to be checked later on.
   */
  @BeforeAll
  static void setupBeforeAll() {
    WeightUpdate constantUpdate = new WeightUpdate();
    WeightUpdate reciprocalUpdate = new WeightUpdate();
    WeightUpdate exponentialUpdate = new WeightUpdate();
    int[] indeces1 = new int[] {0, 0, 1, 0, 0, 2, 0, 0, 1};
    int[] indeces2 = new int[] {1, 2, 2, 2, 3, 3, 1, 3, 3};
    float[] weights = new float[] {-1, -1, -1, -1, -1, -1, 1, 1, 1};
    for (int i = 0; i < 9; i++) {
      constantUpdate.add(indeces1[i], indeces2[i], weights[i]);
      reciprocalUpdate.add(indeces1[i], indeces2[i], weights[i] / 3f);
      exponentialUpdate.add(indeces1[i], indeces2[i], weights[i] / (float) Math.pow(2, 3));
    }
    WEIGHT_UPDATES.put(WeightFactor.CONSTANT, constantUpdate);
    WEIGHT_UPDATES.put(WeightFactor.RECIPROCAL, reciprocalUpdate);
    WEIGHT_UPDATES.put(WeightFactor.EXPONENTIAL, exponentialUpdate);
  }

  /**
   * This method initializes an instance of the <code>VariableInteractionGraph</code> class.
   */
  @BeforeEach
  void setupBeforeEach() {
    vig = new VariableInteractionGraph(INITIAL_FACTOR);
  }

  /**
   * This test processes the clause-updates with each possible weight-factor.
   */
  @Test
  void test_process_eachWeightFactor() {
    for (WeightFactor factor : WeightFactor.values()) {
      vig.setWeightFactor(factor);
      assertEquals(WEIGHT_UPDATES.get(factor), vig.process(clauseUpdates, null));
    }
  }

  /**
   * This test serializes and deserializes the vig and checks, whether the state stays
   * consistent after deserialization.
   */
  @Test
  void test_serializeAndDeserialize_full() {
    for (WeightFactor factor : WeightFactor.values()) {
      vig.setWeightFactor(factor);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      vig.serialize(out);
      ByteArrayInputStream in;
      for (WeightFactor factor2 : WeightFactor.values()) {
        in = new ByteArrayInputStream(out.toByteArray());
        vig.setWeightFactor(factor2);
        vig.deserialize(in);
        assertEquals(factor, vig.getWeightFactor());
      }
    }
  }

}