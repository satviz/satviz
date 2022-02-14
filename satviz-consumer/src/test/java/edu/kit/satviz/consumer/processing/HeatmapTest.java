package edu.kit.satviz.consumer.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HeatmapTest {

  private static final Clause[] CLAUSES = {
      new Clause(new int[] {1, 6, -5, 3}),
      new Clause(new int[] {5, -4, 2}),
      new Clause(new int[] {1, -2, 3}),
      new Clause(new int[] {7, 4, -1}),
      new Clause(new int[] {-6, 2})
  };

  private static final ClauseUpdate[] UPDATES = Arrays.stream(CLAUSES)
      .map(c -> new ClauseUpdate(c, ClauseUpdate.Type.ADD))
      .toArray(ClauseUpdate[]::new);

  private Heatmap heatmap;

  @BeforeEach
  void setUp() {
    heatmap = new Heatmap(3);
  }

  @Test
  void test_process_single() {
    var result = heatmap.process(new ClauseUpdate[] {UPDATES[0]}, null);
    var expected = new HeatUpdate();
    expected.add(0, 0.25f);
    expected.add(5, 0.25f);
    expected.add(4, 0.25f);
    expected.add(2, 0.25f);
    assertEquals(expected, result);
  }

  @Test
  void test_process_multiple_overFull() {
    test_process_multiple_notFull();
    var result = heatmap.process(Arrays.copyOfRange(UPDATES, 2, 5), null);
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
    var result = heatmap.process(Arrays.copyOfRange(UPDATES, 0, 2), null);
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
    var result = heatmap.process(Arrays.copyOfRange(UPDATES, 0, 3), null);
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
    heatmap.process(UPDATES, null);
    heatmap.reset();
    test_process_single();
  }


}
