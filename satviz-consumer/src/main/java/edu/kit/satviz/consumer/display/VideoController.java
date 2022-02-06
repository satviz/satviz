package edu.kit.satviz.consumer.display;

import edu.kit.satviz.consumer.bindings.NativeInvocationException;
import edu.kit.satviz.consumer.bindings.NativeObject;
import edu.kit.satviz.consumer.graph.Graph;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;

public class VideoController extends NativeObject {

  private static final MethodHandle NEW_CONTROLLER = lookupFunction(
      "new_video_controller",
      MethodType.methodType(MemoryAddress.class, MemoryAddress.class, int.class),
      FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_INT)
  );

  private static final MethodHandle RELEASE = lookupFunction(
      "release_video_controller",
      MethodType.methodType(void.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER)
  );

  private static final MethodHandle START_RECORDING = lookupFunction(
      "start_recording",
      MethodType.methodType(int.class, MemoryAddress.class,
          MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER,
          CLinker.C_POINTER, CLinker.C_POINTER)
  );

  private static final MethodHandle STOP_RECORDING = lookupFunction(
      "stop_recording",
      MethodType.methodType(void.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER)
  );

  private static final MethodHandle RESUME_RECORDING = lookupFunction(
      "resume_recording",
      MethodType.methodType(void.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER)
  );

  private static final MethodHandle FINISH_RECORDING = lookupFunction(
      "finish_recording",
      MethodType.methodType(void.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER)
  );

  private VideoController(MemoryAddress pointer) {
    super(pointer);
  }

  public static VideoController create(Graph graph, DisplayType displayType) {
    try {
      MemoryAddress controllerAddr = (MemoryAddress) NEW_CONTROLLER
          .invokeExact(graph.getPointer(), displayType.ordinal());
      return new VideoController(controllerAddr);
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while creating video controller", e);
    }
  }

  @Override
  public void close() {

  }
}
