package edu.kit.satviz.consumer.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import edu.kit.satviz.consumer.config.jsonparsing.ColorDeserializer;
import edu.kit.satviz.consumer.config.jsonparsing.ColorSerializer;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test class tests the gson parsing of a config file.
 */
class JsonConfigParsingTest {

  private static final String CONFIG1_JSON_PATH = "/config1.json";
  private static final String CONFIG2_JSON_PATH = "/config2.json";
  private static final String CONFIG3_JSON_PATH = "/config3.json";

  private static final String VIDEO_TEMPLATE_PATH = "Videos/video-%s.mp4";
  private static final Path INSTANCE_PATH = Paths.get("foo/bar/instance.cnf");
  private static final ConsumerMode CONFIG1_MODE = ConsumerMode.EXTERNAL;
  private static final int CONFIG1_PORT = 12345;
  private static final ConsumerMode CONFIG2_MODE = ConsumerMode.EMBEDDED;
  private static final int CONFIG2_BUFFER_SIZE = 350;
  private static final Path CONFIG2_SOURCE_PATH = Paths.get("foo/bar/solver.so");
  private static final EmbeddedModeSource CONFIG2_SOURCE = EmbeddedModeSource.SOLVER;

  private ObjectMapper mapper;
  private ConsumerConfig config1;
  private ConsumerConfig config2;
  private ConsumerConfig config3;

  /**
   * This set-up method creates a gson parser using the <code>ModeConfigAdapterFactory</code>
   * and <code>PathAdapter</code>. On top of that it creates an instance of the
   * <code>ConsumerConfig</code> class.
   */
  @BeforeEach
  void setUp() {
    SimpleModule pathToStringModule = new SimpleModule("PathToString");
    pathToStringModule.addSerializer(Path.class, new ToStringSerializer());
    SimpleModule colorModule = new SimpleModule("ColorAndHexConverter");
    colorModule.addSerializer(Color.class, new ColorSerializer());
    colorModule.addDeserializer(Color.class, new ColorDeserializer());
    mapper = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);
    mapper.registerModule(pathToStringModule);
    mapper.registerModule(colorModule);
    setUpConfig1();
    setUpConfig2();
    setUpConfig3();
  }

  private void setUpConfig1() {
    ExternalModeConfig modeConfig = new ExternalModeConfig();
    modeConfig.setMode(CONFIG1_MODE);
    modeConfig.setPort(CONFIG1_PORT);
    config1 = new ConsumerConfig();
    config1.setModeConfig(modeConfig);
    config1.setInstancePath(INSTANCE_PATH);
    config1.setBufferSize(100);
    config1.setVideoTemplatePath(VIDEO_TEMPLATE_PATH);
  }

  private void setUpConfig2() {
    EmbeddedModeConfig modeConfig = new EmbeddedModeConfig();
    modeConfig.setMode(CONFIG2_MODE);
    modeConfig.setSource(CONFIG2_SOURCE);
    modeConfig.setSourcePath(CONFIG2_SOURCE_PATH);
    HeatmapColors colors = new HeatmapColors();
    colors.setHotColor(Color.web("#FF0000"));
    colors.setColdColor(Color.web("#0000FF"));
    config2 = new ConsumerConfig();
    config2.setModeConfig(modeConfig);
    config2.setInstancePath(INSTANCE_PATH);
    config2.setVideoTemplatePath(VIDEO_TEMPLATE_PATH);
    config2.setBufferSize(CONFIG2_BUFFER_SIZE);
    Theme theme = new Theme();
    theme.setHeatmapColors(colors);
    config2.setTheme(theme);
  }

  private void setUpConfig3() {
    EmbeddedModeConfig modeConfig = new EmbeddedModeConfig();
    modeConfig.setMode(CONFIG2_MODE);
    modeConfig.setSource(CONFIG2_SOURCE);
    modeConfig.setSourcePath(CONFIG2_SOURCE_PATH);
    HeatmapColors colors = new HeatmapColors();
    colors.setHotColor(Color.web("#FFFFFF"));
    colors.setColdColor(Color.web("#000000"));
    config3 = new ConsumerConfig();
    config3.setModeConfig(modeConfig);
    config3.setInstancePath(INSTANCE_PATH);
    config3.setVideoTemplatePath(VIDEO_TEMPLATE_PATH);
    config3.setBufferSize(CONFIG2_BUFFER_SIZE);
    Theme theme = new Theme();
    theme.setHeatmapColors(colors);
    theme.setBgColor(Color.web("#000000"));
    theme.setEdgeColor(Color.web("#F0F0F0"));
    config3.setTheme(theme);
  }

  /**
   * This tests, whether the parser parses out <code>config1.json</code> correctly.
   */
  @Test
  void deserializeConfiguration_test1() throws IOException {
    ConsumerConfig config = this.mapper.readValue(
        JsonConfigParsingTest.class.getResource(CONFIG1_JSON_PATH),
        ConsumerConfig.class
    );
    assertEquals(config1, config);
  }

  /**
   * This tests, whether the parser parses out <code>config2.json</code> correctly.
   */
  @Test
  void deserializeConfiguration_test2() throws IOException {
    ConsumerConfig config = mapper.readValue(
        JsonConfigParsingTest.class.getResource(CONFIG2_JSON_PATH),
        ConsumerConfig.class
    );
    assertEquals(config2, config);
  }

  /**
   * This tests, whether the parser parses out <code>config2.json</code> correctly.
   */
  @Test
  void deserializeConfiguration_test3() throws IOException {
    ConsumerConfig config = mapper.readValue(
        JsonConfigParsingTest.class.getResource(CONFIG3_JSON_PATH),
        ConsumerConfig.class
    );
    config = config;
    assertEquals(config3, config);
  }

  /**
   * This tests, whether a parser can serialize an instance of the <code>ConsumerConfig</code>
   * class, specifically <code>config1</code>, correctly.
   */
  @Test
  void serializeConfiguration_test1() throws IOException {
    JsonNode node = mapper.readTree(mapper.writeValueAsBytes(config1));
    checkConfigWithExternalProducer(node, config1);
  }

  /**
   * This tests, whether a parser can serialize an instance of the <code>ConsumerConfig</code>
   * class, specifically <code>config2</code>, correctly.
   */
  @Test
  void serializeConfiguration_test2() throws IOException {
    JsonNode jsonObject = mapper.readTree(mapper.writeValueAsBytes(config2));
    checkConfigWithEmbeddedProducer(jsonObject, config2);
  }

  private static void checkConfigWithExternalProducer(JsonNode jsonObj, ConsumerConfig config) {
    checkCommonConfig(jsonObj, config);
    JsonNode modeObject = jsonObj.get("modeConfig");
    ExternalModeConfig modeConfig = (ExternalModeConfig) config.getModeConfig();

    assertEquals(modeConfig.getPort(), modeObject.get("port").intValue());
  }

  private static void checkConfigWithEmbeddedProducer(JsonNode jsonObj, ConsumerConfig config) {
    checkCommonConfig(jsonObj, config);
    JsonNode modeObject = jsonObj.get("modeConfig");
    EmbeddedModeConfig modeConfig = (EmbeddedModeConfig) config.getModeConfig();

    assertEquals(modeConfig.getSource().name(), modeObject.get("source").textValue());
    assertEquals(modeConfig.getSourcePath(), Paths.get(modeObject.get("sourcePath").textValue()));
  }

  private static void checkCommonConfig(JsonNode jsonConfig, ConsumerConfig config) {
    assertEquals(
            config.isNoGui(),
            jsonConfig.get("noGui").booleanValue()
    );
    assertEquals(
            config.getVideoTemplatePath(),
            jsonConfig.get("videoTemplatePath").textValue()
    );
    assertEquals(
            config.getBufferSize(),
            jsonConfig.get("bufferSize").intValue()
    );
    assertEquals(
            config.getWindowSize(),
            jsonConfig.get("windowSize").intValue()
    );
    assertEquals(
            config.getInstancePath(),
            Paths.get(jsonConfig.get("instancePath").textValue())
    );
    assertEquals(
            config.isRecordImmediately(),
            jsonConfig.get("recordImmediately").booleanValue()
    );
    assertEquals(
            config.getModeConfig().getMode().name(),
            jsonConfig.get("modeConfig").get("mode").textValue()
    );
  }

  /**
   * First serializes and then deserializes an instance of the <code>ConsumerConfig</code> class.
   */
  @Test
  void serializeThenDeserializeConfiguration_test() throws IOException {
    Reader reader = new StringReader(mapper.writeValueAsString(config1));
    ConsumerConfig config = mapper.readValue(reader, ConsumerConfig.class);
    assertEquals(config1, config);
    reader.close();

    reader = new StringReader(mapper.writeValueAsString(config2));
    config = mapper.readValue(reader, ConsumerConfig.class);
    assertEquals(config2, config);
    reader.close();
  }

}
