#ifndef SATVIZ_THEORA_ENCODER_HPP_
#define SATVIZ_THEORA_ENCODER_HPP_

#include <satviz/VideoEncoder.hpp>

namespace satviz {
namespace video {

struct TheoraStream;

/**
 * A concrete implementation of a VideoEncoder that outputs OGG/Theora video files.
 */
class TheoraEncoder : public VideoEncoder {
private:
  TheoraStream *stream = nullptr;

public:
  TheoraEncoder();
  ~TheoraEncoder() override;
  bool startRecording(const char *filename, int width, int height) override;
  void submitFrame(VideoFrame &frame, bool last) override;
};

} // namespace video
} // namespace satviz

#endif
