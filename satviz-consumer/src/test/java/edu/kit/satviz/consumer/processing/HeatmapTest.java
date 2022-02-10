package edu.kit.satviz.consumer.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
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
    expected.add(1, 0.25f);
    expected.add(6, 0.25f);
    expected.add(5, 0.25f);
    expected.add(3, 0.25f);
    assertEquals(expected, result);
  }

  @Test
  void test_reset() {
    heatmap.process(UPDATES, null);
    heatmap.reset();
    test_process_single();
  }


}
