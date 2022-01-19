package edu.kit.satviz.consumer.config;

import java.nio.file.Path;

public class ConsumerConfig {

  private boolean noGui;
  private String videoTemplatePath;
  private int bufferSize;
  private WeightFactor weightFactor;
  private int windowSize;
  private HeatmapColors heatmapColors;
  private ConsumerModeConfig modeConfig;
  private Path instancePath;
  private boolean recordImmediately;

  public void setNoGui(boolean noGui) {
    this.noGui = noGui;
  }

  public void setVideoTemplatePath(String videoTemplatePath) {
    this.videoTemplatePath = videoTemplatePath;
  }

  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  public void setWeightFactor(WeightFactor weightFactor) {
    this.weightFactor = weightFactor;
  }

  public void setWindowSize(int windowSize) {
    this.windowSize = windowSize;
  }

  public void setHeatmapColors(HeatmapColors heatmapColors) {
    this.heatmapColors = heatmapColors;
  }

  public void setModeConfig(ConsumerModeConfig modeConfig) {
    this.modeConfig = modeConfig;
  }

  public void setInstancePath(Path instancePath) {
    this.instancePath = instancePath;
  }

  public void setRecordImmediately(boolean recordImmediately) {
    this.recordImmediately = recordImmediately;
  }

  public boolean isNoGui() {
    return noGui;
  }

  public String getVideoTemplatePath() {
    return videoTemplatePath;
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

  public boolean isRecordImmediately() {
    return recordImmediately;
  }

  public Path getInstancePath() {
    return instancePath;
  }
}
