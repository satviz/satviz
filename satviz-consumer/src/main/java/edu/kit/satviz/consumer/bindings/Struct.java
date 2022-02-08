package edu.kit.satviz.consumer.bindings;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

public class Struct {

  private final MemoryLayout layout;
  private final Map<String, VarHandle> fields;

  public Struct(MemoryLayout layout, List<Field> fields) {
    this.layout = layout;
    this.fields = new HashMap<>(fields.size());
    for (Field field : fields) {
      this.fields.put(
          field.name(),
          layout.varHandle(field.javaType(), PathElement.groupElement(field.name()))
      );
    }
  }

  public VarHandle varHandle(String name) {
    return fields.get(name);
  }

  public MemoryLayout getLayout() {
    return layout;
  }

  public MemorySegment allocateNew(ResourceScope scope) {
    return MemorySegment.allocateNative(layout, scope);
  }

  public static Builder builder() {
    return new Builder();
  }

  public record Field(String name, Class<?> javaType, MemoryLayout layout) {

  }

  public static class Builder {

    private final List<Field> fields = new ArrayList<>();

    public Builder field(String name, Class<?> javaType, MemoryLayout layout) {
      fields.add(new Field(name, javaType, layout));
      return this;
    }

    public Struct build() {
      MemoryLayout structLayout = NativeObject.paddedStruct(
          fields.stream()
              .map(Field::layout)
              .toArray(MemoryLayout[]::new)
      );
      return new Struct(structLayout, fields);
    }

  }
}
