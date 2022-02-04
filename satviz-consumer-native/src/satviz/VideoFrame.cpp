#include <satviz/VideoFrame.hpp>
#include <satviz/YCbCr.hpp>

#include <cstddef>
#include <cstdint>

namespace satviz {
namespace video {

VideoFrame::VideoFrame(int width, int height)
  : width(width), height(height), stride(width) {
  Y  = new unsigned char[(size_t) width * (size_t) height];
  Cb = new unsigned char[(size_t) width * (size_t) height];
  Cr = new unsigned char[(size_t) width * (size_t) height];
}

VideoFrame::~VideoFrame() {
  delete[] Y;
  delete[] Cb;
  delete[] Cr;
}

VideoFrame VideoFrame::fromBgraImage(int width, int height, const void *data) {
  VideoFrame frame(width, height);
  const uint32_t *pixels = (const uint32_t *) data;
  size_t num_pixels = (size_t) width * (size_t) height;
  unsigned prev = ~pixels[0];
  for (size_t i = 0; i < num_pixels; i++) {
    unsigned pixel = pixels[i];
    if (pixel == prev) {
      // Optimization: encoding runs of the same color without having to constantly re-convert
      frame.Y [i] = frame.Y [i-1];
      frame.Cb[i] = frame.Cb[i-1];
      frame.Cr[i] = frame.Cr[i-1];
    } else {
      unsigned B = (pixel >>  0) & 0xFF;
      unsigned G = (pixel >>  8) & 0xFF;
      unsigned R = (pixel >> 16) & 0xFF;
      frame.Y [i] = (unsigned char) rgbToY (R, G, B);
      frame.Cb[i] = (unsigned char) rgbToCb(R, G, B);
      frame.Cr[i] = (unsigned char) rgbToCr(R, G, B);
    }
  }
  return frame;
}

} // namespace video
} // namespace satviz
