package edu.kit.satviz.consumer.config.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import edu.kit.satviz.consumer.config.ConsumerMode;
import edu.kit.satviz.consumer.config.ConsumerModeConfig;
import edu.kit.satviz.consumer.config.EmbeddedModeConfig;
import edu.kit.satviz.consumer.config.ExternalModeConfig;
import java.io.IOException;

public class ModeConfigAdapterFactory implements TypeAdapterFactory {

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    if (!ConsumerModeConfig.class.isAssignableFrom(type.getRawType())) {
      return null;
    }

    TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
    return new TypeAdapter<>() {
      @Override
      public void write(JsonWriter out, T value) throws IOException {
        delegate.write(out, value);
      }

      @SuppressWarnings("unchecked")
      @Override
      public T read(JsonReader in) throws IOException {
        JsonElement element = gson.fromJson(in, JsonElement.class);
        ConsumerMode mode = ConsumerMode.valueOf(
            element.getAsJsonObject().get("mode").getAsString()
        );
        Class<? extends ConsumerModeConfig> cls = switch (mode) {
          case EXTERNAL -> ExternalModeConfig.class;
          case EMBEDDED -> EmbeddedModeConfig.class;
        };
        TypeAdapter<? extends ConsumerModeConfig> adapter = gson.getDelegateAdapter(
            ModeConfigAdapterFactory.this, TypeToken.get(cls)
        );

        return (T) adapter.read(in);
      }
    };
  }
}
