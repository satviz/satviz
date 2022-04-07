package edu.kit.satviz.consumer.config;

import edu.kit.satviz.consumer.config.routines.NullRoutine;
import edu.kit.satviz.consumer.config.routines.Routine;
import edu.kit.satviz.consumer.processing.Heatmap;
import edu.kit.satviz.consumer.processing.HeatmapImplementation;
import edu.kit.satviz.consumer.processing.VariableInteractionGraph;
import edu.kit.satviz.consumer.processing.VariableInteractionGraphImplementation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * This class contains the starting configuration for the consumer.
 */
public class ConsumerConfig {

  public static final boolean DEFAULT_NO_GUI = false;
  public static final String DEFAULT_VIDEO_TEMPLATE_PATH;

  static {
    String path = System.getProperty("user.home") + "/satviz/recordings/";

    try {
      Files.createDirectories(Path.of(path));
    } catch (IOException e) {
      e.printStackTrace();
    }

    DEFAULT_VIDEO_TEMPLATE_PATH = path + System.currentTimeMillis() + "video-{}.ogv";
  }

  public static final boolean DEFAULT_RECORD_IMMEDIATELY = false;
  public static final int DEFAULT_BUFFER_SIZE = 10;
  public static final int STEP_AMOUNT_BUFFER_SIZE = 10;
  public static final WeightFactor DEFAULT_WEIGHT_FACTOR = WeightFactor.RECIPROCAL;
  public static final int MIN_BUFFER_SIZE = 1;
  public static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE;
  public static final int MIN_WINDOW_SIZE = 1;
  public static final int MAX_WINDOW_SIZE = Integer.MAX_VALUE;
  public static final int DEFAULT_WINDOW_SIZE = 1000;
  public static final int STEP_AMOUNT_WINDOW_SIZE = 100;
  public static final int MIN_CONTRACTION_ITERATIONS = 0;
  public static final int MAX_CONTRACTION_ITERATIONS = Integer.MAX_VALUE;
  public static final int DEFAULT_CONTRACTION_ITERATIONS = 0;
  public static final int STEP_AMOUNT_CONTRACTION_ITERATIONS = 1;
  public static final long DEFAULT_PERIOD = 33;
  public static final int DEFAULT_VIDEO_TIMEOUT = 60;
  public static final ConsumerMode DEFAULT_CONSUMER_MODE = ConsumerMode.EXTERNAL;
  public static final double STEP_AMOUNT_FACTOR_PROCESSED_CLAUSES = 0.05;

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
  private HeatmapImplementation heatmapImplementation = Heatmap.DEFAULT_IMPLEMENTATION;
  private int windowSize = DEFAULT_WINDOW_SIZE;
  private VariableInteractionGraphImplementation vigImplementation =
      VariableInteractionGraph.DEFAULT_IMPLEMENTATION;
  private int contractionIterations = DEFAULT_CONTRACTION_ITERATIONS;
  private long period = DEFAULT_PERIOD;
  private int videoTimeout = DEFAULT_VIDEO_TIMEOUT;
  private Theme theme = new Theme();
  private Routine relayoutRoutine = new NullRoutine();

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
   * Setter-method for the heatmap implementation.
   *
   * @param heatmapImplementation The heatmap implementation.
   */
  public void setHeatmapImplementation(HeatmapImplementation heatmapImplementation) {
    this.heatmapImplementation = heatmapImplementation;
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
   * Setter-method for the theme (heatmap colors + background + ...).
   *
   * @param theme An instance of the {@code Theme} class.
   */
  public void setTheme(Theme theme) {
    this.theme = theme;
  }

  /**
   * Setter-method for the variable interaction graph implementation.
   *
   * @param vigImplementation The variable interaction graph implementation.
   */
  public void setVigImplementation(VariableInteractionGraphImplementation vigImplementation) {
    this.vigImplementation = vigImplementation;
  }

  /**
   * Setter-method for the number of iterations for the graph contraction.
   *
   * @param contractionIterations The number of iterations the graph contraction
   *                              algorithm is supposed to do.
   */
  public void setContractionIterations(int contractionIterations) {
    this.contractionIterations = contractionIterations;
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
   * Setter-method for the desired length of the recorded video (in seconds).
   *
   * @param videoTimeout The desired length of the video.
   */
  public void setVideoTimeout(int videoTimeout) {
    this.videoTimeout = videoTimeout;
  }

  /**
   * Setter-method for the relayout routine.
   *
   * @param relayoutRoutine Instance of the {@code Routine} class.
   */
  public void setRelayoutRoutine(Routine relayoutRoutine) {
    this.relayoutRoutine = relayoutRoutine;
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
   * Getter-method for the heatmap implementation.
   *
   * @return The heatmap implementation.
   */
  public HeatmapImplementation getHeatmapImplementation() {
    return heatmapImplementation;
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
   * Getter-method for the theme (heatmap colors + background + ...).
   *
   * @return An instance of the {@code Theme} class.
   */
  public Theme getTheme() {
    return theme;
  }

  /**
   * Getter-method for the variable interaction graph implementation.
   *
   * @return The variable interaction graph implementation.
   */
  public VariableInteractionGraphImplementation getVigImplementation() {
    return vigImplementation;
  }

  /**
   * Getter-method for the number of iterations for the graph contraction.
   *
   * @return The number of iterations the graph contraction algorithm is supposed to do.
   */
  public int getContractionIterations() {
    return contractionIterations;
  }

  /**
   * Getter-method for the minimal time period in ms between advancing the animation.
   *
   * @return The minimal time period in ms between advancing the animation.
   */
  public long getPeriod() {
    return period;
  }

  /**
   * Getter-method for the desired length of the recorded video (in seconds).
   *
   * @return The desired length of the video.
   */
  public int getVideoTimeout() {
    return videoTimeout;
  }

  /**
   * Getter-method for the relayout routine.
   *
   * @return Instance of the {@code Routine} class.
   */
  public Routine getRelayoutRoutine() {
    return relayoutRoutine;
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
        && Objects.equals(theme, config.theme)
        && heatmapImplementation == config.heatmapImplementation
        && vigImplementation == config.vigImplementation
        && contractionIterations == config.contractionIterations
        && period == config.period
        && videoTimeout == config.videoTimeout
        && Objects.equals(relayoutRoutine, config.relayoutRoutine);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modeConfig, instancePath, noGui, videoTemplatePath,
        recordImmediately, bufferSize, weightFactor, heatmapImplementation, windowSize, theme,
        vigImplementation, contractionIterations, period, videoTimeout, relayoutRoutine);
  }

}
