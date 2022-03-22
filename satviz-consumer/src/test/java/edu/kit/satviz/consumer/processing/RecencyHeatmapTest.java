package edu.kit.satviz.consumer.processing;

import static edu.kit.satviz.consumer.processing.Constants.UPDATES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecencyHeatmapTest {

  private Heatmap heatmap;

  @BeforeEach
  void setUp() {
    heatmap = new RecencyHeatmap(3);
  }

  @Test
  void test_process_single() {
    var result = heatmap.process(
        new ClauseUpdate[] {UPDATES[0]}, null, IdentityMapping.INSTANCE
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
        Arrays.copyOfRange(UPDATES, 2, 5), null, IdentityMapping.INSTANCE
    );
    var expected = new HeatUpdate();
    expected.add(0, 2f / 3);
    expected.add(1, 1);
    expected.add(2, 1f / 3);
    expected.add(3, 2f / 3);
    expected.add(4, 0);
    expected.add(5, 1);
    expected.add(6, 2f / 3);
    assertEquals(expected, result);
  }

  @Test
  void test_process_multiple_notFull() {
    var result = heatmap.process(
        Arrays.copyOfRange(UPDATES, 0, 2), null, IdentityMapping.INSTANCE
    );
    var expected = new HeatUpdate();
    expected.add(0, 2f / 3);
    expected.add(1, 1);
    expected.add(2, 2f / 3);
    expected.add(3, 1);
    expected.add(4, 1);
    expected.add(5, 2f / 3);
    assertEquals(expected, result);
  }

  @Test
  void test_process_multiple_full() {
    var result = heatmap.process(
        Arrays.copyOfRange(UPDATES, 0, 3), null, IdentityMapping.INSTANCE
    );
    var expected = new HeatUpdate();
    expected.add(0, 1);
    expected.add(1, 1);
    expected.add(2, 1);
    expected.add(3, 2f / 3);
    expected.add(4, 2f / 3);
    expected.add(5, 1f / 3);
    assertEquals(expected, result);
  }

  @Test
  void test_reset() {
    heatmap.process(UPDATES, null, IdentityMapping.INSTANCE);
    heatmap.reset();
    var result = heatmap.process(new ClauseUpdate[] {UPDATES[0]}, null, IdentityMapping.INSTANCE);
    var expected = new HeatUpdate();
    expected.add(0, 1);
    expected.add(1, 0);
    expected.add(2, 1);
    expected.add(3, 0);
    expected.add(4, 1);
    expected.add(5, 1);
    expected.add(6, 0);
    assertEquals(expected, result);
  }

}
