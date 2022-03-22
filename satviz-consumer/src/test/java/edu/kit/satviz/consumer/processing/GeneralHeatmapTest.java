package edu.kit.satviz.consumer.processing;

import static edu.kit.satviz.consumer.processing.Constants.UPDATES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.kit.satviz.consumer.graph.HeatUpdate;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeneralHeatmapTest {

  private Heatmap heatmap;

  @BeforeEach
  void setUp() {
    heatmap = new RecencyHeatmap(3);
  }

  @Test
  void test_setHeatmapSize_shrinkBeforeCursor() {
    heatmap.process(UPDATES, null, IdentityMapping.INSTANCE);
    heatmap.setHeatmapSize(2);
    var result = heatmap.process(Arrays.copyOfRange(UPDATES, 0, 1), null, IdentityMapping.INSTANCE);
    var expected = new HeatUpdate();
    expected.add(0, 1);
    expected.add(2, 1);
    expected.add(4, 1);
    expected.add(5, 1);
    expected.add(1, 1f / 2);
    expected.add(6, 0);
    expected.add(3, 0);
    assertEquals(expected, result);
  }

  @Test
  void test_setHeatmapSize_shrinkAfterCursor() {
    heatmap.process(UPDATES, null, IdentityMapping.INSTANCE);
    heatmap.process(UPDATES, null, IdentityMapping.INSTANCE);
    heatmap.setHeatmapSize(2);
    var result = heatmap.process(Arrays.copyOfRange(UPDATES, 0, 1), null, IdentityMapping.INSTANCE);
    var expected = new HeatUpdate();
    expected.add(0, 1);
    expected.add(2, 1);
    expected.add(4, 1);
    expected.add(5, 1);
    expected.add(1, 1f / 2);
    expected.add(6, 0);
    expected.add(3, 0);
    assertEquals(expected, result);
  }

  @Test
  void test_setHeatmapSize_grow() {
    heatmap.process(UPDATES, null, IdentityMapping.INSTANCE);
    heatmap.setHeatmapSize(5);
    var result = heatmap.process(Arrays.copyOfRange(UPDATES, 0, 1), null, IdentityMapping.INSTANCE);
    var expected = new HeatUpdate();
    expected.add(0, 1);
    expected.add(2, 1);
    expected.add(4, 1);
    expected.add(5, 1);
    expected.add(1, 4f / 5);
    expected.add(3, 3f / 5);
    expected.add(6, 3f / 5);
    assertEquals(expected, result);
  }

}
