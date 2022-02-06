#include <satviz/VideoFrame.hpp>
#include <satviz/YCbCr.hpp>

#include <cstddef>
#include <cstdint>

namespace satviz {
namespace video {

VideoFrame::VideoFrame(const VideoGeometry &geom)
  : geom(geom) {
  size_t num_pixels = (size_t) geom.padded_width * (size_t) geom.padded_height;
  Y  = new unsigned char[num_pixels];
  Cb = new unsigned char[num_pixels];
  Cr = new unsigned char[num_pixels];
}

VideoFrame::~VideoFrame() {
  delete[] Y;
  delete[] Cb;
  delete[] Cr;
}

VideoFrame VideoFrame::fromBgraImage(const VideoGeometry &geom, const void *data) {
  VideoFrame frame(geom);
  const uint32_t *pixels = (const uint32_t *) data;
  size_t row_start = geom.view_offset_x + geom.view_offset_y * geom.padded_width;
  unsigned prev = ~pixels[0];
  for (unsigned r = 0; r < geom.view_height; r++) {
    for (unsigned c = 0; c < geom.view_width; c++) {
      size_t i = row_start + c;
      unsigned pixel = pixels[i];
      if (pixel == prev) {
        // Optimization: encoding runs of the same color without having to constantly re-convert
        frame.Y [i] = frame.Y [i - 1];
        frame.Cb[i] = frame.Cb[i - 1];
        frame.Cr[i] = frame.Cr[i - 1];
      } else {
        unsigned B = (pixel >>  0) & 0xFF;
        unsigned G = (pixel >>  8) & 0xFF;
        unsigned R = (pixel >> 16) & 0xFF;
        frame.Y [i] = (unsigned char) rgbToY (R, G, B);
        frame.Cb[i] = (unsigned char) rgbToCb(R, G, B);
        frame.Cr[i] = (unsigned char) rgbToCr(R, G, B);
      }
    }
    row_start += geom.padded_width;
  }
  return frame;
}

} // namespace video
} // namespace satviz
