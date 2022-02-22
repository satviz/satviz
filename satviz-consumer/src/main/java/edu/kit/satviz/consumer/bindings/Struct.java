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

/**
 * A utility class that can be used to describe and access a C struct concisely.
 *
 * @see #builder()
 */
public final class Struct {

  private final MemoryLayout layout;
  private final Map<String, VarHandle> fields;

  private Struct(MemoryLayout layout, List<Field> fields) {
    this.layout = layout;
    this.fields = new HashMap<>(fields.size());
    for (Field field : fields) {
      this.fields.put(
          field.name(),
          layout.varHandle(field.javaType(), PathElement.groupElement(field.name()))
      );
    }
  }

  /**
   * Returns a {@code VarHandle} to the field of the given name.
   *
   * @param name The name of the field contained in this struct.
   * @return A {@code VarHandle} which can be used to get and set values for this field.
   */
  public VarHandle varHandle(String name) {
    return fields.get(name);
  }

  /**
   * Returns the layout of this struct.
   *
   * @return The {@code MemoryLayout} describing this struct.
   */
  public MemoryLayout getLayout() {
    return layout;
  }

  /**
   * Allocates a segment of memory that fits one instance of this struct.
   *
   * @param scope the scope of the resulting segment
   * @return a {@link MemorySegment} whose size is equal to that of this struct.
   */
  public MemorySegment allocateNew(ResourceScope scope) {
    return MemorySegment.allocateNative(layout, scope);
  }

  /**
   * Returns a builder that can be used to create a {@code Struct} instance.
   *
   * @return A new {@link Builder} object.
   */
  public static Builder builder() {
    return new Builder();
  }

  private record Field(String name, Class<?> javaType, MemoryLayout layout) {

  }

  /**
   * A builder class for {@link Struct}.
   */
  public static final class Builder {

    private final List<Field> fields = new ArrayList<>();

    /**
     * Add a struct field.
     *
     * @param name The name of the field.
     * @param javaType The java representation of the field type.
     * @param layout The memory layout of the field type.
     * @return {@code this}
     */
    public Builder field(String name, Class<?> javaType, MemoryLayout layout) {
      fields.add(new Field(name, javaType, layout));
      return this;
    }

    /**
     * Finish {@code Struct} construction.
     *
     * @return a {@link Struct} that gives access to all the fields added by this builder.
     */
    public Struct build() {
      MemoryLayout structLayout = NativeObject.paddedStruct(
          fields.stream()
              .map(field -> field.layout().withName(field.name()))
              .toArray(MemoryLayout[]::new)
      );
      return new Struct(structLayout, fields);
    }

  }
}
