#ifndef SATVIZ_VIDEO_FRAME_HPP_
#define SATVIZ_VIDEO_FRAME_HPP_

namespace satviz {
namespace video {

/**
 *
 */
struct VideoFrame {
  int width;
  int height;
  int stride;
  unsigned char *Y;
  unsigned char *Cb;
  unsigned char *Cr;

  VideoFrame(int width, int height);
};

} // namespace video
} // namespace satviz

#endif
