#ifndef SATVIZ_VIDEO_CONTROLLER_HPP_
#define SATVIZ_VIDEO_CONTROLLER_HPP_

#include <satviz/Graph.hpp>
#include <satviz/Display.hpp>
#include <satviz/VideoEncoder.hpp>

namespace satviz {
namespace video {

/**
 *
 */
class VideoController {
public:
  VideoController(Graph *graph, Display *dpy);

  void nextFrame();

  bool startRecording(const char *filename, VideoEncoder *enc);
  void stopRecording();
  void resumeRecording();
  void finishRecording();
};

} // namespace video
} // namespace satviz

#endif
