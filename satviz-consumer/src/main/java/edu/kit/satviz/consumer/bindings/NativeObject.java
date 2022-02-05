package edu.kit.satviz.consumer.bindings;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import jdk.incubator.foreign.*;

public abstract class NativeObject implements AutoCloseable {

  private static final String PREFIX = "satviz_";

  static {
    try {
      NativeLibraryLoader.loadLibrary("/satviz-consumer-native.so");
    } catch (IOException e) {
      throw new InitializationError("Could not load required native library", e);
    }
  }

  protected final MemoryAddress pointer;

  protected NativeObject(MemoryAddress pointer) {
    this.pointer = pointer;
  }

  protected static MethodHandle lookupFunction(
      String name, MethodType methodType, FunctionDescriptor descriptor
  ) {
    String fullName = PREFIX + name;
    var address = SymbolLookup.loaderLookup().lookup(fullName)
        .orElseThrow(() -> new UnsatisfiedLinkError("Could not find C function " + fullName));
    return CLinker.getInstance().downcallHandle(address, methodType, descriptor);
  }

  public static MemoryLayout withPadding(MemoryLayout... fields) {
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


}
