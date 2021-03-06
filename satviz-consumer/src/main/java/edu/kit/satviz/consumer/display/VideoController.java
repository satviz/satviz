package edu.kit.satviz.consumer.display;

import edu.kit.satviz.consumer.bindings.NativeInvocationException;
import edu.kit.satviz.consumer.bindings.NativeObject;
import edu.kit.satviz.consumer.config.Theme;
import edu.kit.satviz.consumer.graph.Graph;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

/**
 * Class used to render and record the visualisation done by satviz.
 * Every instance of this class is bound to a {@code satviz::video::VideoController}
 * instance in C++.
 */
public class VideoController extends NativeObject {

  private static final MethodHandle NEW_CONTROLLER = lookupFunction(
      "new_video_controller",
      MethodType.methodType(MemoryAddress.class, MemoryAddress.class,
          int.class, int.class, int.class),
      FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER,
          CLinker.C_INT, CLinker.C_INT, CLinker.C_INT)
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

  private static final MethodHandle RESET_CAMERA = lookupFunction(
      "reset_camera",
      MethodType.methodType(void.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER)
  );

  private static final MethodHandle NEXT_FRAME = lookupFunction(
      "next_frame",
      MethodType.methodType(void.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER)
  );

  private static final MethodHandle APPLY_THEME = lookupFunction(
      "apply_theme",
      MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER)
  );

  private VideoController(MemoryAddress pointer) {
    super(pointer);
  }

  /**
   * Create a new {@code VideoController}. This will also create a display window.
   *
   * @param graph The graph used for the visualisation.
   * @param displayType The type of display to use (see {@link DisplayType})
   * @param width The width of the display
   * @param height The height of the display
   * @return a {@code VideoController} instance
   */
  public static VideoController create(
      Graph graph, DisplayType displayType, int width, int height
  ) {
    try {
      MemoryAddress controllerAddr = (MemoryAddress) NEW_CONTROLLER
          .invokeExact(graph.getPointer(), displayType.ordinal(), width, height);
      if (MemoryAddress.NULL.equals(controllerAddr)) {
        throw new AssertionError("invalid display type provided");
      }
      return new VideoController(controllerAddr);
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while creating video controller", e);
    }
  }

  /**
   * Start recording the visualisation and save it to the file with the given name.
   *
   * @param fileName The path to the output file.
   * @param encoder The name of the video encoder to use. Currently supported encoders:
   *                <ul>
   *                  <li>{@code "theora"}
   *                  - <a href="https://en.wikipedia.org/wiki/Theora">Link</a></li>
   *                </ul>
   * @return {@code true} if the recording could be started successfully, {@code false} if not.
   */
  public boolean startRecording(String fileName, String encoder) {
    try (ResourceScope local = ResourceScope.newConfinedScope()) {
      int res = (int) START_RECORDING.invokeExact(
          getPointer(),
          CLinker.toCString(fileName, local).address(),
          CLinker.toCString(encoder, local).address()
      );
      if (res == -1) {
        throw new IllegalArgumentException("Unsupported encoder " + encoder);
      }
      return res != 0;
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while starting a recording", e);
    }
  }

  /**
   * Stop the active recording session ("Pause").
   */
  public void stopRecording() {
    try {
      STOP_RECORDING.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while stopping a recording", e);
    }
  }

  /**
   * Resume the active recording session.
   */
  public void resumeRecording() {
    try {
      RESUME_RECORDING.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while resuming a recording", e);
    }
  }

  /**
   * Finish the active recording session, finalising the video output file.
   */
  public void finishRecording() {
    try {
      FINISH_RECORDING.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while finishing a recording", e);
    }
  }

  /**
   * Resets the camera to include the entire graph.
   */
  public void resetCamera() {
    try {
      RESET_CAMERA.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while resetting camera", e);
    }
  }

  /**
   * Progress to the next frame in the visualisation.
   */
  public void nextFrame() {
    try {
      NEXT_FRAME.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while progressing to next frame", e);
    }
  }

  /**
   * Applies the contents of a Theme to the running visualization.
   * @param theme the theme to apply
   */
  public void applyTheme(Theme theme) {
    try (ResourceScope local = ResourceScope.newConfinedScope()) {
      MemorySegment segment = theme.toSegment(local);
      APPLY_THEME.invokeExact(getPointer(), segment.address());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while applying theme", e);
    }
  }

  /**
   * Destroy the underlying resources.<br>
   * To clean up {@code VideoController} instances, you should generally
   * use {@link #close()} instead.
   */
  public void destroy() {
    try {
      RELEASE.invokeExact(getPointer());
    } catch (Throwable e) {
      throw new NativeInvocationException("Error while destroying video controller", e);
    }
  }

  @Override
  public void close() {
    destroy();
  }
}
