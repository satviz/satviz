#ifndef SATVIZ_VIDEO_CONTROLLER_HPP_
#define SATVIZ_VIDEO_CONTROLLER_HPP_

#include <satviz/Graph.hpp>
#include <satviz/Display.hpp>
#include <satviz/VideoEncoder.hpp>

namespace satviz {
namespace video {

/**
 * High-level object for managing & controlling the video module.
 */
class VideoController {
private:
  graph::Graph *graph;
  Display *display;

public:
  bool wantToClose;

  VideoController(graph::Graph *gr, Display *dpy);
  ~VideoController();

  /**
   * Process a new frame.
   *
   * This entails reacting to user input, redrawing the visuals, etc.
   */
  void nextFrame();

  bool startRecording(const char *filename, VideoEncoder *enc);
  void stopRecording();
  void resumeRecording();
  void finishRecording();
};

} // namespace video
} // namespace satviz

#endif
