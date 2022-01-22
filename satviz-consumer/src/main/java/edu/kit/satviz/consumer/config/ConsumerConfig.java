package edu.kit.satviz.consumer.config;

import java.nio.file.Path;
import java.util.Objects;

public class ConsumerConfig {

  public static final boolean DEFAULT_NO_GUI = false;
  public static final String DEFAULT_VIDEO_TEMPLATE_PATH = "/cool"; // DEFINITELY NEED TO CHANGE!
  public static final boolean DEFAULT_RECORD_IMMEDIATELY = false;
  public static final int DEFAULT_BUFFER_SIZE = 100;
  public static final WeightFactor DEFAULT_WEIGHT_FACTOR = WeightFactor.RECIPROCAL;
  public static final int DEFAULT_WINDOW_SIZE = 1000;

  // mandatory settings
  private ConsumerModeConfig modeConfig;
  private Path instancePath;

  // workflow settings
  private boolean noGui = DEFAULT_NO_GUI;
  private String videoTemplatePath = DEFAULT_VIDEO_TEMPLATE_PATH;
  private boolean recordImmediately = DEFAULT_RECORD_IMMEDIATELY;

  // cosmetic settings
  private int bufferSize = DEFAULT_BUFFER_SIZE;
  private WeightFactor weightFactor = DEFAULT_WEIGHT_FACTOR;
  private int windowSize = DEFAULT_WINDOW_SIZE;
  private HeatmapColors heatmapColors = new HeatmapColors(); // this contains the default colors


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
    return noGui == config.noGui
        && recordImmediately == config.recordImmediately
        && bufferSize == config.bufferSize
        && windowSize == config.windowSize
        && Objects.equals(modeConfig, config.modeConfig)
        && Objects.equals(instancePath, config.instancePath)
        && Objects.equals(videoTemplatePath, config.videoTemplatePath)
        && weightFactor == config.weightFactor
        && Objects.equals(heatmapColors, config.heatmapColors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modeConfig, instancePath, noGui, videoTemplatePath,
        recordImmediately, bufferSize, weightFactor, windowSize, heatmapColors);
  }

}
