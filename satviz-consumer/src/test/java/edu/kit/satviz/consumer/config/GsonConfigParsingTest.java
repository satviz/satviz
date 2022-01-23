package edu.kit.satviz.consumer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.kit.satviz.consumer.config.json.ModeConfigAdapterFactory;
import edu.kit.satviz.consumer.config.json.PathAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class tests the gson parsing of a config file.
 */
class GsonConfigParsingTest {

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

    ExternalModeConfig modeConfig = new ExternalModeConfig();
    modeConfig.setPort(12345);
    modeConfig.setMode(ConsumerMode.EXTERNAL);
    this.config1 = new ConsumerConfig();
    this.config1.setModeConfig(modeConfig);
    this.config1.setInstancePath(Paths.get("foo/bar/instance.cnf"));
    this.config1.setVideoTemplatePath("Videos/video-%s.mp4");
  }

  /**
   * This tests, whether the parser parses out <code>config1.json</code> correctly.
   */
  @Test
  void deserialize_configuration_test() {
    try (Reader reader = new InputStreamReader(
            GsonConfigParsingTest.class.getResourceAsStream("/config1.json")
    )){
      ConsumerConfig config = this.gson.fromJson(reader, ConsumerConfig.class);
      assertEquals(config1, config);
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Not really a test but can be used to print out a serialized configuration.
   */
  @Test
  void serialize_configuration_test() {
    System.out.println(this.gson.toJson(this.config1));
  }

}
