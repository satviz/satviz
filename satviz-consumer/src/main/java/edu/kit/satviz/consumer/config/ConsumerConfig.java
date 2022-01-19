package edu.kit.satviz.consumer.config;

import java.nio.file.Path;

public class ConsumerConfig {

  private boolean noGui;

  private String resultTemplate;

  private int bufferSize;

  private WeightFactor weightFactor;

  private int windowSize;

  private HeatmapColors heatmapColors;

  private ConsumerModeConfig modeConfig;

  public boolean isNoGui() {
    return noGui;
  }

  public String getResultTemplate() {
    return resultTemplate;
  }

  public int getBufferSize() {
    return bufferSize;
  }

  public WeightFactor getWeightFactor() {
    return weightFactor;
  }

  public int getWindowSize() {
    return windowSize;
  }

  public HeatmapColors getHeatmapColors() {
    return heatmapColors;
  }

  public ConsumerModeConfig getModeConfig() {
    return modeConfig;
  }


}
