package edu.kit.satviz.consumer.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;
import java.util.Objects;

/**
 * This class contains the starting configuration for the consumer.
 */
public class ConsumerConfig {

  public static final boolean DEFAULT_NO_GUI = false;
  // DEFINITELY NEED TO CHANGE!
  public static final String DEFAULT_VIDEO_TEMPLATE_PATH = "/cool/yes.ogv";
  public static final boolean DEFAULT_RECORD_IMMEDIATELY = false;
  public static final int DEFAULT_BUFFER_SIZE = 10;
  public static final WeightFactor DEFAULT_WEIGHT_FACTOR = WeightFactor.RECIPROCAL;
  public static final int MIN_WINDOW_SIZE = 0;
  public static final int MAX_WINDOW_SIZE = Integer.MAX_VALUE;
  public static final int DEFAULT_WINDOW_SIZE = 1000;
  public static final long DEFAULT_PERIOD = 33;
  public static final ConsumerMode DEFAULT_CONSUMER_MODE = ConsumerMode.EXTERNAL;

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
  private long period = DEFAULT_PERIOD;


  /**
   * Setter-method for an instance of the <code>ConsumerModeConfig</code> class.
   *
   * @param modeConfig An instance of the <code>ConsumerModeConfig</code> class.
   */
  public void setModeConfig(ConsumerModeConfig modeConfig) {
    this.modeConfig = modeConfig;
  }

  /**
   * Setter-method for the path of the SAT-instance.
   *
   * @param instancePath The path of the SAT-instance.
   */
  public void setInstancePath(Path instancePath) {
    this.instancePath = instancePath;
  }

  /**
   * Setter-method for whether the animation should be started with GUI or without.
   *
   * @param noGui <i>true</i>, if the animation should run with GUI,<br>
   *              <i>false</i>, if not.
   */
  public void setNoGui(boolean noGui) {
    this.noGui = noGui;
  }

  /**
   * Setter-method for the template-path for the storage of recorded videos.
   *
   * @param videoTemplatePath The template-path for the storage of recorded videos.
   */
  public void setVideoTemplatePath(String videoTemplatePath) {
    this.videoTemplatePath = videoTemplatePath;
  }

  /**
   * Setter-method for whether the animation should be recorded immediately or not.
   *
   * @param recordImmediately <i>true</i>, if the animation should be recorded immediately,<br>
   *                          <i>false</i>, if not.
   */
  public void setRecordImmediately(boolean recordImmediately) {
    this.recordImmediately = recordImmediately;
  }

  /**
   * Setter-method for the buffer-size.
   *
   * @param bufferSize The size of the buffer for the incoming clauses.
   */
  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  /**
   * Setter-method for the weight factor.
   *
   * @param weightFactor An instance of the <code>WeightFactor</code> enum.
   */
  public void setWeightFactor(WeightFactor weightFactor) {
    this.weightFactor = weightFactor;
  }

  /**
   * Setter-method for the window size.
   *
   * @param windowSize The size of the moving window for the heatmap.
   */
  public void setWindowSize(int windowSize) {
    this.windowSize = windowSize;
  }

  /**
   * Setter-method for the heatmap colors.
   *
   * @param heatmapColors An instance of the <code>HeatmapColors</code> class.
   */
  public void setHeatmapColors(HeatmapColors heatmapColors) {
    this.heatmapColors = heatmapColors;
  }

  /**
   * Setter-method for the minimal time period in ms between advancing the animation.
   *
   * @param period The minimal time period in ms between advancing the animation.
   */
  public void setPeriod(long period) {
    this.period = period;
  }

  /**
   * Getter-method for more settings set within an instance of
   * the <code>ConsumerModeConfig</code> class.
   *
   * @return An instance of the <code>ConsumerModeConfig</code> class.
   */
  public ConsumerModeConfig getModeConfig() {
    return modeConfig;
  }

  /**
   * Getter-method for the path of the SAT-instance.
   *
   * @return The path of the SAT-instance.
   */
  public Path getInstancePath() {
    return instancePath;
  }

  /**
   * Getter-method for whether the animation should be started with GUI or without.
   *
   * @return <i>true</i>, if the animation should run with GUI,<br>
   *         <i>false</i>, if not.
   */
  public boolean isNoGui() {
    return noGui;
  }

  /**
   * Getter-method for the template-path for the storage of recorded videos.
   *
   * @return The template-path for the storage of recorded videos.
   */
  public String getVideoTemplatePath() {
    return videoTemplatePath;
  }

  /**
   * Getter-method for whether the animation should be recorded immediately or not.
   *
   * @return <i>true</i>, if the animation should be recorded immediately,<br>
   *         <i>false</i>, if not.
   */
  public boolean isRecordImmediately() {
    return recordImmediately;
  }

  /**
   * Getter-method for the buffer-size.
   *
   * @return The size of the buffer for the incoming clauses.
   */
  public int getBufferSize() {
    return bufferSize;
  }

  /**
   * Getter-method for the weight factor.
   *
   * @return An instance of the <code>WeightFactor</code> enum.
   */
  public WeightFactor getWeightFactor() {
    return weightFactor;
  }

  /**
   * Getter-method for the window size.
   *
   * @return The size of the moving window for the heatmap.
   */
  public int getWindowSize() {
    return windowSize;
  }

  /**
   * Getter-method for the heatmap colors.
   *
   * @return An instance of the <code>HeatmapColors</code> class.
   */
  public HeatmapColors getHeatmapColors() {
    return heatmapColors;
  }

  /**
   * Getter-method for the minimal time period in ms between advancing the animation.
   *
   * @return The minimal time period in ms between advancing the animation.
   */
  public long getPeriod() {
    return period;
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
        && Objects.equals(heatmapColors, config.heatmapColors)
        && period == config.period;
  }

  @Override
  public int hashCode() {
    return Objects.hash(modeConfig, instancePath, noGui, videoTemplatePath,
        recordImmediately, bufferSize, weightFactor, windowSize, heatmapColors, period);
  }

}
