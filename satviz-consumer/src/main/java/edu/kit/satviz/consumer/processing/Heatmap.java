package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.HeatUpdate;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.Serializer;

public class Heatmap implements ClauseUpdateProcessor {

  private int[] ringBuffer;
  private int heatmapSize;

  public void setHeatmapSize(int heatmapSize) {
    this.heatmapSize = heatmapSize;
  }

  public int getHeatmapSize() {
    return heatmapSize;
  }

  @Override
  public HeatUpdate process(ClauseUpdate[] updates, Graph graph) {
    return null;
  }

  @Override
  public Serializer<Heatmap> serializer() {
    return null;
  }

}
