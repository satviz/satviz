#ifndef SATVIZ_VIDEO_CONTROLLER_HPP_
#define SATVIZ_VIDEO_CONTROLLER_HPP_

#include <satviz/Graph.hpp>
#include <satviz/Display.hpp>

namespace satviz {
namespace video {

/**
 *
 */
class VideoController {

public:
  VideoController();

  void nextFrame();

  bool startRecording();

  void stopRecording();

  void resumeRecording();

  void finishRecording();
};

} // namespace video
} // namespace satviz

#endif
