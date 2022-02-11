#ifndef SATVIZ_VIDEO_FRAME_HPP_
#define SATVIZ_VIDEO_FRAME_HPP_

namespace satviz {
namespace video {

/**
 * Holds geometric information about the currently recorded video.
 */
struct VideoGeometry {
  /// width with extra padding around the border (needed by encoder)
  unsigned padded_width;
  /// height with extra padding around the border (needed by encoder)
  unsigned padded_height;
  /// width of the usable video region
  unsigned view_width;
  /// height of the usable video region
  unsigned view_height;
  /// x offset of the usable video region from the padded border
  unsigned view_offset_x;
  /// y offset of the usable video region from the padded border
  unsigned view_offset_y;
};

/**
 * Holds the visual information of a single frame of a video.
 *
 * Pixel values are represented in the YCbCr color space.
 * The Y, Cb, and Cr components are stored as separate color planes (separate arrays).
 */
struct VideoFrame {
  const VideoGeometry &geom;
  unsigned char *Y;
  unsigned char *Cb;
  unsigned char *Cr;

  explicit VideoFrame(const VideoGeometry &geom);
  ~VideoFrame();

  unsigned getStride() const { return geom.padded_width; }

  /**
   * Create a VideoFrame from an 8-bit-per-channel BGRA image.
   * @param geom the size information of this frame
   * @param data the pixels values of the image
   * @return     a new VideoFrame
   */
  static VideoFrame fromBgraImage(const VideoGeometry &geom, const void *pixels);
};

} // namespace video
} // namespace satviz

#endif
