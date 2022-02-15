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
  unsigned prev_pixel   = ~pixels[0];
  size_t   prev_out_idx = -1;
  for (unsigned r = 0; r < geom.view_height; r++) {
    for (unsigned c = 0; c < geom.view_width; c++) {
      size_t in_idx  = r * geom.view_width + c;
      size_t out_idx = (r + geom.view_offset_y) * geom.padded_width + (c + geom.view_offset_x);
      unsigned pixel = pixels[in_idx];
      if (pixel == prev_pixel) {
        // Optimization: encoding runs of the same color without having to constantly re-convert
        frame.Y [out_idx] = frame.Y [prev_out_idx];
        frame.Cb[out_idx] = frame.Cb[prev_out_idx];
        frame.Cr[out_idx] = frame.Cr[prev_out_idx];
      } else {
        unsigned B = (pixel >>  0) & 0xFF;
        unsigned G = (pixel >>  8) & 0xFF;
        unsigned R = (pixel >> 16) & 0xFF;
        frame.Y [out_idx] = (unsigned char) rgbToY (R, G, B);
        frame.Cb[out_idx] = (unsigned char) rgbToCb(R, G, B);
        frame.Cr[out_idx] = (unsigned char) rgbToCr(R, G, B);
      }
      prev_pixel   = pixel;
      prev_out_idx = out_idx;
    }
  }
  return frame;
}

} // namespace video
} // namespace satviz
