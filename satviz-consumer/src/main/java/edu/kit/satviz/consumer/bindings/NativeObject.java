package edu.kit.satviz.consumer.bindings;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.SymbolLookup;

/**
 * A class whose instances are coupled to some native memory, such as a C++ object or struct.
 */
public abstract class NativeObject implements AutoCloseable {

  private static final String PREFIX = "satviz_";

  static {
    try {
      // to add windows support, select the correct shared library here
      NativeLibraryLoader.loadLibrary("/libsatviz-consumer-native.so");
    } catch (IOException e) {
      throw new NativeInitializationError("Could not load required native library", e);
    }
  }

  private final MemoryAddress pointer;

  /**
   * Initializes this {@code NativeObject} with the given pointer to native memory.
   *
   * @param pointer The underlying memory address of this object's native component.
   */
  protected NativeObject(MemoryAddress pointer) {
    this.pointer = pointer;
  }

  /**
   * Looks up a satviz C function.
   *
   * @param name The name of the function, without the "satviz_" prefix.
   * @param methodType the corresponding Java method signature
   * @param descriptor the C function signature
   * @return a {@code MethodHandle} to the looked up function.
   * @throws UnsatisfiedLinkError if no function matching the given description could be found.
   */
  public static MethodHandle lookupFunction(
      String name, MethodType methodType, FunctionDescriptor descriptor
  ) {
    String fullName = PREFIX + name;
    var address = SymbolLookup.loaderLookup().lookup(fullName)
        .orElseThrow(() -> new UnsatisfiedLinkError("Could not find C function " + fullName));
    return CLinker.getInstance().downcallHandle(address, methodType, descriptor);
  }

  /**
   * Creates a {@code structLayout} with the given fields, adding padding where necessary
   * (as per the C standard).
   *
   * @param fields The field layouts of the struct.
   * @return A {@code MemoryLayout} for a struct.
   */
  // courtesy of my friend Joshua:
  // https://github.com/IGJoshua/coffi/blob/master/src/clj/coffi/layout.clj
  public static MemoryLayout paddedStruct(MemoryLayout... fields) {
    long offset = 0;
    List<MemoryLayout> alignedFields = new ArrayList<>();
    for (MemoryLayout field : fields) {
      long size = field.byteSize();
      long r = offset % field.byteAlignment();
      if (r > 0) {
        offset += size - r;
        alignedFields.add(MemoryLayout.paddingLayout(size - r));
      }
      offset += size;
      alignedFields.add(field);
    }
    long strongestAlignment = MemoryLayout.structLayout(fields).byteAlignment();
    long r = offset % strongestAlignment;
    if (r > 0) {
      alignedFields.add(MemoryLayout.paddingLayout(strongestAlignment - r));
    }
    return MemoryLayout.structLayout(alignedFields.toArray(new MemoryLayout[0]));
  }

  /**
   * Returns the underlying pointer to the native component of this object.
   *
   * @return a {@code MemoryAddress} to native memory.
   */
  public MemoryAddress getPointer() {
    return pointer;
  }

  @Override
  public abstract void close();
}
