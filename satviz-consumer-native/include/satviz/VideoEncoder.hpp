#ifndef SATVIZ_VIDEO_ENCODER_HPP_
#define SATVIZ_VIDEO_ENCODER_HPP_

#include <satviz/VideoFrame.hpp>

#include <fstream>

namespace satviz {
namespace video {

/**
 * An abstract base class for video encoding backends.
 */
class VideoEncoder {
protected:
  std::ofstream file;
  VideoGeometry geom;

public:
  virtual ~VideoEncoder() = default;
  virtual bool startRecording(const char *filename, int width, int height) = 0;
  virtual void submitFrame(VideoFrame &frame, bool last) = 0;
  const VideoGeometry &getGeometry() { return geom; }
};

} // namespace video
} // namespace satviz

#endif
