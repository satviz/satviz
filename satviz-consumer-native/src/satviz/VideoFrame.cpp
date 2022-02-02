#include <satviz/VideoFrame.hpp>
#include <satviz/YCbCr.hpp>

#include <cstddef>

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

VideoFrame VideoFrame::fromImage(int width, int height, unsigned char *pixels) {
  VideoFrame frame(width, height);
  size_t num_pixels = (size_t) width * (size_t) height;
  for (size_t i = 0; i < num_pixels; i++) {
    // TODO optimize for repeating pixel values
    int R = pixels[4*i+0];
    int G = pixels[4*i+1];
    int B = pixels[4*i+2];
    frame.Y [i] = (unsigned char) rgbToY (R, G, B);
    frame.Cb[i] = (unsigned char) rgbToCr(R, G, B);
    frame.Cr[i] = (unsigned char) rgbToCb(R, G, B);
  }
  return frame;
}

} // namespace video
} // namespace satviz
