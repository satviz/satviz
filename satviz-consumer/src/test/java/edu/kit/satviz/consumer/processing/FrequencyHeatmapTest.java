package edu.kit.satviz.consumer.processing;

import static edu.kit.satviz.consumer.processing.Constants.UPDATES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Arrays;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FrequencyHeatmapTest {

  private static final IntUnaryOperator DEFAULT_NODE_MAPPING = IdentityMapping.INSTANCE;

  private Heatmap heatmap;

  @BeforeEach
  void setUp() {
    heatmap = new FrequencyHeatmap(3, FrequencyHeatmap.HeatStrategy.SIZE);
  }

  @Test
  void test_process_single() {
    var result = heatmap.process(
        new ClauseUpdate[] {UPDATES[0]}, null, DEFAULT_NODE_MAPPING
    );
    var expected = new HeatUpdate();
    expected.add(0, 1);
    expected.add(5, 1);
    expected.add(4, 1);
    expected.add(2, 1);
    assertEquals(expected, result);
  }

  @Test
  void test_process_multiple_overFull() {
    test_process_multiple_notFull();
    var result = heatmap.process(
        Arrays.copyOfRange(UPDATES, 2, 5), null, DEFAULT_NODE_MAPPING
    );
    var expected = new HeatUpdate();
    expected.add(0, 2f / 3); // 1: 2
    expected.add(1, 2f / 3); // 2: 2
    expected.add(2, 1f / 3); // 3: 1
    expected.add(3, 1f / 3); // 4: 1
    expected.add(4, 0); // 5: 0
    expected.add(5, 1f / 3); // 6: 1
    expected.add(6, 1f / 3); // 7: 1
    assertEquals(expected, result);
  }

  @Test
  void test_process_multiple_notFull() {
    var result = heatmap.process(
        Arrays.copyOfRange(UPDATES, 0, 2), null, DEFAULT_NODE_MAPPING
    );
    var expected = new HeatUpdate();
    expected.add(0, 1f / 2);
    expected.add(1, 1f / 2);
    expected.add(2, 1f / 2);
    expected.add(3, 1f / 2);
    expected.add(4, 1);
    expected.add(5, 1f / 2);
    assertEquals(expected, result);
  }

  @Test
  void test_process_multiple_full() {
    var result = heatmap.process(
        Arrays.copyOfRange(UPDATES, 0, 3), null, DEFAULT_NODE_MAPPING
    );
    var expected = new HeatUpdate();
    expected.add(0, 2f / 3); // 0: 2,
    expected.add(1, 2f / 3); // 1: 2,
    expected.add(2, 2f / 3); // 2: 2,
    expected.add(3, 1f / 3); // 3: 1,
    expected.add(4, 2f / 3); // 4: 2,
    expected.add(5, 1f / 3); // 5: 1
    assertEquals(expected, result);
  }

  @Test
  void test_reset() {
    heatmap.process(UPDATES, null, DEFAULT_NODE_MAPPING);
    heatmap.reset();
    test_process_single();
  }


}
