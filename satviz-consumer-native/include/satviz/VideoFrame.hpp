#ifndef SATVIZ_VIDEO_FRAME_HPP_
#define SATVIZ_VIDEO_FRAME_HPP_

namespace satviz {
namespace video {

/**
 * Holds the visual information of a single frame of a video.
 *
 * Pixel values are represented in the YCbCr color space.
 * The Y, Cb, and Cr components are stored as separate color planes (separate arrays).
 */
struct VideoFrame {
  const int width;
  const int height;
  const int stride;
  unsigned char *Y;
  unsigned char *Cb;
  unsigned char *Cr;

  VideoFrame(int width, int height);
  ~VideoFrame();

  /**
   * Create a VideoFrame from an 8-bit-per-channel RGBA image.
   * @param width  the width of the image
   * @param height the height of the image
   * @param pixels the pixels values of the image
   * @return       a new VideoFrame
   */
  static VideoFrame fromImage(int width, int height, unsigned char *pixels);
};

} // namespace video
} // namespace satviz

#endif
