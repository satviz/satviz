package edu.kit.satviz.consumer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.kit.satviz.consumer.config.json.ModeConfigAdapterFactory;
import edu.kit.satviz.consumer.config.json.PathAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test class tests the gson parsing of a config file.
 */
class GsonConfigParsingTest {

  private static final String CONFIG1_JSON_PATH = "/config1.json";
  private static final String CONFIG2_JSON_PATH = "/config2.json";

  private static final String VIDEO_TEMPLATE_PATH = "Videos/video-%s.mp4";
  private static final Path INSTANCE_PATH = Paths.get("foo/bar/instance.cnf");
  private static final ConsumerMode CONFIG1_MODE = ConsumerMode.EXTERNAL;
  private static final int CONFIG1_PORT = 12345;
  private static final ConsumerMode CONFIG2_MODE = ConsumerMode.EMBEDDED;
  private static final int CONFIG2_BUFFER_SIZE = 350;
  private static final int CONFIG2_FROM_COLOR = 12;
  private static final int CONFIG2_TO_COLOR = 10;
  private static final Path CONFIG2_SOURCE_PATH = Paths.get("foo/bar/solver.so");
  private static final EmbeddedModeSource CONFIG2_SOURCE = EmbeddedModeSource.SOLVER;

  private Gson gson;
  private ConsumerConfig config1;
  private ConsumerConfig config2;

  /**
   * This set-up method creates a gson parser using the <code>ModeConfigAdapterFactory</code>
   * and <code>PathAdapter</code>. On top of that it creates an instance of the
   * <code>ConsumerConfig</code> class.
   */
  @BeforeEach
  void setUp() {
    this.gson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapterFactory(new ModeConfigAdapterFactory())
            .registerTypeHierarchyAdapter(Path.class, new PathAdapter()).create();


    setUpConfig2();
    setUpConfig1();
  }

  private void setUpConfig1() {
    ExternalModeConfig modeConfig = new ExternalModeConfig();
    modeConfig.setMode(CONFIG1_MODE);
    modeConfig.setPort(CONFIG1_PORT);
    config1 = new ConsumerConfig();
    config1.setModeConfig(modeConfig);
    config1.setInstancePath(INSTANCE_PATH);
    config1.setVideoTemplatePath(VIDEO_TEMPLATE_PATH);
  }

  private void setUpConfig2() {
    EmbeddedModeConfig modeConfig = new EmbeddedModeConfig();
    modeConfig.setMode(CONFIG2_MODE);
    modeConfig.setSource(CONFIG2_SOURCE);
    modeConfig.setSourcePath(CONFIG2_SOURCE_PATH);
    HeatmapColors colors = new HeatmapColors();
    colors.setFromColor(CONFIG2_FROM_COLOR);
    colors.setToColor(CONFIG2_TO_COLOR);
    config2 = new ConsumerConfig();
    config2.setModeConfig(modeConfig);
    config2.setInstancePath(INSTANCE_PATH);
    config2.setVideoTemplatePath(VIDEO_TEMPLATE_PATH);
    config2.setBufferSize(CONFIG2_BUFFER_SIZE);
    config2.setHeatmapColors(colors);
  }

  /**
   * This tests, whether the parser parses out <code>config1.json</code> correctly.
   */
  @Test
  void deserializeConfiguration_test1() throws IOException {
    Reader reader = new InputStreamReader(
            GsonConfigParsingTest.class.getResourceAsStream(CONFIG1_JSON_PATH)
    );
    ConsumerConfig config = this.gson.fromJson(reader, ConsumerConfig.class);
    assertEquals(config1, config);
    reader.close();
  }

  /**
   * This tests, whether the parser parses out <code>config2.json</code> correctly.
   */
  @Test
  void deserializeConfiguration_test2() throws IOException {
    Reader reader = new InputStreamReader(
            GsonConfigParsingTest.class.getResourceAsStream(CONFIG2_JSON_PATH)
    );
    ConsumerConfig config = gson.fromJson(reader, ConsumerConfig.class);
    assertEquals(config2, config);
    reader.close();
  }

  /**
   * This tests, whether a parser can serialize an instance of the <code>ConsumerConfig</code>
   * class, specifically <code>config1</code>, correctly.
   */
  @Test
  void serializeConfiguration_test1() {
    JsonObject jsonObject = JsonParser.parseString(gson.toJson(config1)).getAsJsonObject();
    checkConfigWithExternalProducer(jsonObject, config1);
  }

  /**
   * This tests, whether a parser can serialize an instance of the <code>ConsumerConfig</code>
   * class, specifically <code>config2</code>, correctly.
   */
  @Test
  void serializeConfiguration_test2() {
    JsonObject jsonObject = JsonParser.parseString(gson.toJson(config2)).getAsJsonObject();
    checkConfigWithEmbeddedProducer(jsonObject, config2);
  }

  private static void checkConfigWithExternalProducer(JsonObject jsonObj, ConsumerConfig config) {
    checkCommonConfig(jsonObj, config);
    JsonObject modeObject = jsonObj.get("modeConfig").getAsJsonObject();
    ExternalModeConfig modeConfig = (ExternalModeConfig) config.getModeConfig();

    assertEquals(modeConfig.getPort(), modeObject.get("port").getAsInt());
  }

  private static void checkConfigWithEmbeddedProducer(JsonObject jsonObj, ConsumerConfig config) {
    checkCommonConfig(jsonObj, config);
    JsonObject modeObject = jsonObj.get("modeConfig").getAsJsonObject();
    EmbeddedModeConfig modeConfig = (EmbeddedModeConfig) config.getModeConfig();

    assertEquals(modeConfig.getSource().name(), modeObject.get("source").getAsString());
    assertEquals(modeConfig.getSourcePath(), Paths.get(modeObject.get("sourcePath").getAsString()));
  }

  private static void checkCommonConfig(JsonObject jsonConfig, ConsumerConfig config) {
    assertEquals(
            config.isNoGui(),
            jsonConfig.get("noGui").getAsBoolean()
    );
    assertEquals(
            config.getVideoTemplatePath(),
            jsonConfig.get("videoTemplatePath").getAsString()
    );
    assertEquals(
            config.getBufferSize(),
            jsonConfig.get("bufferSize").getAsInt()
    );
    assertEquals(
            config.getWindowSize(),
            jsonConfig.get("windowSize").getAsInt()
    );
    assertEquals(
            config.getInstancePath(),
            Paths.get(jsonConfig.get("instancePath").getAsString())
    );
    assertEquals(
            config.isRecordImmediately(),
            jsonConfig.get("recordImmediately").getAsBoolean()
    );
    assertEquals(
            config.getModeConfig().getMode().name(),
            jsonConfig.get("modeConfig").getAsJsonObject().get("mode").getAsString()
    );
  }

  /**
   * First serializes and then deserializes an instance of the <code>ConsumerConfig</code> class.
   */
  @Test
  void serializeThenDeserializeConfiguration_test() throws IOException {
    Reader reader = new StringReader(gson.toJson(config1));
    ConsumerConfig config = gson.fromJson(reader, ConsumerConfig.class);
    assertEquals(config1, config);
    reader.close();

    reader = new StringReader(gson.toJson(config2));
    config = gson.fromJson(reader, ConsumerConfig.class);
    assertEquals(config2, config);
    reader.close();
  }

}
