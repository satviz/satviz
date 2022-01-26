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

  private static final String VIDEO_TEMPLATE_PATH = "Videos/video-%s.mp4";
  private static final Path INSTANCE_PATH = Paths.get("foo/bar/instance.cnf");
  private static final ConsumerMode MODE = ConsumerMode.EXTERNAL;
  private static final int PORT = 12345;

  private Gson gson;
  private ConsumerConfig config1;

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

    ExternalModeConfig modeConfig = new ExternalModeConfig();
    modeConfig.setPort(PORT);
    modeConfig.setMode(MODE);
    this.config1 = new ConsumerConfig();
    this.config1.setModeConfig(modeConfig);
    this.config1.setInstancePath(INSTANCE_PATH);
    this.config1.setVideoTemplatePath(VIDEO_TEMPLATE_PATH);
  }

  /**
   * This tests, whether the parser parses out <code>config1.json</code> correctly.
   */
  @Test
  void deserializeConfiguration_test() throws IOException {
    Reader reader = new InputStreamReader(
            GsonConfigParsingTest.class.getResourceAsStream("/config1.json")
    );
    ConsumerConfig config = this.gson.fromJson(reader, ConsumerConfig.class);
    assertEquals(config1, config);
    reader.close();
  }

  /**
   * This tests, whether a parser can serialize an instance of the <code>ConsumerConfig</code>
   * class correctly.
   */
  @Test
  void serializeConfiguration_test() {
    JsonObject jsonObject = JsonParser.parseString(gson.toJson(config1)).getAsJsonObject();

    assertEquals(
            config1.isNoGui(),
            jsonObject.get("noGui").getAsBoolean()
    );
    assertEquals(
            config1.getVideoTemplatePath(),
            jsonObject.get("videoTemplatePath").getAsString()
    );
    assertEquals(
            config1.getBufferSize(),
            jsonObject.get("bufferSize").getAsInt()
    );
    assertEquals(
            config1.getWindowSize(),
            jsonObject.get("windowSize").getAsInt()
    );
    assertEquals(
            config1.getInstancePath(),
            Paths.get(jsonObject.get("instancePath").getAsString())
    );
    assertEquals(
            config1.isRecordImmediately(),
            jsonObject.get("recordImmediately").getAsBoolean()
    );

    JsonObject modeObject = jsonObject.get("modeConfig").getAsJsonObject();
    assertEquals(
            config1.getModeConfig().getMode().name(),
            modeObject.get("mode").getAsString()
    );
    assertEquals(PORT, modeObject.get("port").getAsInt());
  }

  /**
   * First serializes and then deserializes an instance of the <code>ConsumerConfig</code> class.
   */
  @Test
  void serializeThenDeserializeConfiguration_test() throws IOException {
    Reader reader = new StringReader(this.gson.toJson(this.config1));
    ConsumerConfig config = this.gson.fromJson(reader, ConsumerConfig.class);
    assertEquals(config1, config);
    reader.close();
  }

}
