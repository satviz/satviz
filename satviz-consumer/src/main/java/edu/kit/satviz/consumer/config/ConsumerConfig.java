package edu.kit.satviz.consumer.config;

import java.nio.file.Path;

public class ConsumerConfig {

  // manditory settings
  private ConsumerModeConfig modeConfig;
  private Path instancePath;

  // workflow settings
  private boolean noGui;
  private String videoTemplatePath;
  private boolean recordImmediately;

  // cosmetic settings
  private int bufferSize;
  private WeightFactor weightFactor;
  private int windowSize;
  private HeatmapColors heatmapColors;


  public void setModeConfig(ConsumerModeConfig modeConfig) {
    this.modeConfig = modeConfig;
  }

  public void setInstancePath(Path instancePath) {
    this.instancePath = instancePath;
  }

  public void setNoGui(boolean noGui) {
    this.noGui = noGui;
  }

  public void setVideoTemplatePath(String videoTemplatePath) {
    this.videoTemplatePath = videoTemplatePath;
  }

  public void setRecordImmediately(boolean recordImmediately) {
    this.recordImmediately = recordImmediately;
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


  public ConsumerModeConfig getModeConfig() {
    return modeConfig;
  }

  public Path getInstancePath() {
    return instancePath;
  }

  public boolean isNoGui() {
    return noGui;
  }

  public String getVideoTemplatePath() {
    return videoTemplatePath;
  }

  public boolean isRecordImmediately() {
    return recordImmediately;
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsumerConfig config = (ConsumerConfig) o;

    boolean equalManditorySettings = modeConfig.equals(config.getModeConfig())
                    && instancePath.equals(config.getInstancePath());
    boolean equalWorkflowSettings = noGui == config.isNoGui()
                    && videoTemplatePath.equals(config.getVideoTemplatePath())
                    && recordImmediately == config.isRecordImmediately();
    boolean equalCosmeticSettings = bufferSize == config.getBufferSize()
                    && weightFactor.equals(config.getWeightFactor())
                    && windowSize == config.getWindowSize()
                    && heatmapColors.equals(config.getHeatmapColors());
    return equalManditorySettings && equalWorkflowSettings && equalCosmeticSettings;
  }

  @Override
  public int hashCode() {
    // TODO
    return super.hashCode();
  }

}
