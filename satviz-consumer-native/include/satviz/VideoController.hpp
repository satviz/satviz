#ifndef SATVIZ_VIDEO_CONTROLLER_HPP_
#define SATVIZ_VIDEO_CONTROLLER_HPP_

#include <satviz/Graph.hpp>
#include <satviz/Display.hpp>
#include <satviz/GraphRenderer.hpp>
#include <satviz/VideoEncoder.hpp>

namespace satviz {
namespace video {

/**
 * High-level object for managing & controlling the video module.
 */
class VideoController {
private:
  enum RecState {
    REC_OFF,
    REC_ON,
    REC_PAUSED,
    REC_WINDUP,
    REC_WINDDOWN,
  };

  graph::Graph &graph;
  Display *display;
  GraphRenderer *renderer;
  Camera camera;
  enum RecState recording_state = REC_OFF;
  VideoEncoder *video_encoder = nullptr;

  bool mouse_grabbed = false;
  int  mouse_x;
  int  mouse_y;

  void processEvent(sf::Event &event);

public:
  bool wantToClose = false;

  VideoController(graph::Graph &gr, Display *dpy);
  ~VideoController();

  /**
   * Applies the contents of a Theme to the running visualization.
   * @param theme the theme to apply
   */
  void applyTheme(const Theme &theme) { renderer->applyTheme(theme); }

  /**
   * Move the camera so that the entire graph is in view.
   */
  void resetCamera();

  /**
   * Process a new frame.
   *
   * This entails reacting to user input, redrawing the visuals, etc.
   */
  void nextFrame();

  /**
   * Start recording the visualization as a video.
   * @param filename where the recording should be written to
   * @param enc the video encoder that should be used
   * @return false if any errors occur, true otherwise
   */
  bool startRecording(const char *filename, VideoEncoder *enc);
  /**
   * Temporarily stop the recording.
   */
  void stopRecording();
  /**
   * Resume recording if it has been stopped.
   */
  void resumeRecording();
  /**
   * Finish recording altogether and finalize the generated video file.
   */
  void finishRecording();
};

} // namespace video
} // namespace satviz

#endif
