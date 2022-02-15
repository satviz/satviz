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

class VariableInteractionGraphTest {

  private static final WeightFactor INITIAL_FACTOR = WeightFactor.CONSTANT;

  /**
   *         -1 0
   * d -1 -2  3 0
   * d -1 -3 -4 0
   * d -1  2  4 0
   *          2 0
   *            0
   */
  private static final ClauseUpdate[] clauseUpdates = new ClauseUpdate[] {
          new ClauseUpdate(new Clause(new int[]{-1}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{-1, -2, 3}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{-1, -3, -4}), ClauseUpdate.Type.REMOVE),
          new ClauseUpdate(new Clause(new int[]{-1, 2, 4}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{2}), ClauseUpdate.Type.ADD),
          new ClauseUpdate(new Clause(new int[]{}), ClauseUpdate.Type.ADD),
  };

  private static final Map<WeightFactor, WeightUpdate> map = new HashMap<>();

  private VariableInteractionGraph vig;

  @BeforeAll
  static void setupBeforeAll() {
    WeightUpdate constantUpdate = new WeightUpdate();
    constantUpdate.add(-1, -2, -1);
    constantUpdate.add(-1, 3, -1);
    constantUpdate.add(-2, 3, -1);
    constantUpdate.add(-1, -3, -1);
    constantUpdate.add(-1, -4, -1);
    constantUpdate.add(-3, -4, -1);
    constantUpdate.add(-1, 2, 1);
    constantUpdate.add(-1, 4, 1);
    constantUpdate.add(2, 4, 1);
    map.put(WeightFactor.CONSTANT, constantUpdate);

    WeightUpdate reciprocalUpdate = new WeightUpdate();
    constantUpdate.add(-1, -2, -1f / 3f);
    constantUpdate.add(-1, 3, -1f / 3f);
    constantUpdate.add(-2, 3, -1f / 3f);
    constantUpdate.add(-1, -3, -1f / 3f);
    constantUpdate.add(-1, -4, -1f / 3f);
    constantUpdate.add(-3, -4, -1f / 3f);
    constantUpdate.add(-1, 2, 1f / 3f);
    constantUpdate.add(-1, 4, 1f / 3f);
    constantUpdate.add(2, 4, 1f / 3f);
    map.put(WeightFactor.RECIPROCAL, reciprocalUpdate);

    WeightUpdate exponentialUpdate = new WeightUpdate();
    constantUpdate.add(-1, -2, -1f / (float) Math.pow(2, 3));
    constantUpdate.add(-1, 3, -1f / (float) Math.pow(2, 3));
    constantUpdate.add(-2, 3, -1f / (float) Math.pow(2, 3));
    constantUpdate.add(-1, -3, -1f / (float) Math.pow(2, 3));
    constantUpdate.add(-1, -4, -1f / (float) Math.pow(2, 3));
    constantUpdate.add(-3, -4, -1f / (float) Math.pow(2, 3));
    constantUpdate.add(-1, 2, 1f / (float) Math.pow(2, 3));
    constantUpdate.add(-1, 4, 1f / (float) Math.pow(2, 3));
    constantUpdate.add(2, 4, 1f / (float) Math.pow(2, 3));
    map.put(WeightFactor.EXPONENTIAL, exponentialUpdate);
  }

  @BeforeEach
  void setupBeforeEach() {
    vig = new VariableInteractionGraph(INITIAL_FACTOR);
  }

  @Test
  void test_process_eachWeightFactor() {
    for (WeightFactor factor : WeightFactor.values()) {
      vig.setWeightFactor(factor);
      assertEquals(map.get(factor), vig.process(clauseUpdates, null));
    }
  }

  @Test
  void test_serializeAndDeserialize_full() {
    for (WeightFactor factor : WeightFactor.values()) {
      vig.setWeightFactor(factor);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      vig.serialize(out);
      ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
      vig.deserialize(in);
      assertEquals(factor, vig.getWeightFactor());
    }
  }

}