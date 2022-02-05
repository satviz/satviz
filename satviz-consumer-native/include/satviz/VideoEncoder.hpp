#ifndef SATVIZ_VIDEO_ENCODER_HPP_
#define SATVIZ_VIDEO_ENCODER_HPP_

#include <satviz/VideoFrame.hpp>

#include <fstream>

namespace satviz {
namespace video {

/**
 *
 */
class VideoEncoder {
protected:
  std::ofstream file;
  int width;
  int height;

public:
  virtual ~VideoEncoder() {}
  virtual bool startRecording(const char *filename, int width, int height) = 0;
  virtual void submitFrame(VideoFrame &frame, bool last) = 0;
};

} // namespace video
} // namespace satviz

#endif
