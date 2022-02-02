#include <gtest/gtest.h>
#include <satviz/VideoFrame.hpp>
#include <satviz/YCbCr.hpp>
#include <cstdio>

using namespace ::satviz::video;

/*
 * Test whether VideoFrame::fromImage() correctly converts images to video frames.
 */
TEST(VideoFrame, FromImage) {
  const int width  = 1;
  const int height = 4;
  // RGBA pixel colors in HTML encoding
  // We use strings instead of literals integer constants to avoid endianness problems
  const char *colors[width * height] = {
      "#FF000000",
      "#FFFF0000",
      "#00FFFF00",
      "#FF0000A0",
  };

  // convert color codes to pixel values
  unsigned char pixels[4 * width * height];
  for (int i = 0; i < width * height; i++) {
    unsigned r, g, b, a;
    sscanf(colors[i], "#%2x%2x%2x%2x", &r, &g, &b, &a);
    pixels[4*i+0] = (unsigned char) r;
    pixels[4*i+1] = (unsigned char) g;
    pixels[4*i+2] = (unsigned char) b;
    pixels[4*i+3] = (unsigned char) a;
  }

  // Create VideoFrame from image data
  VideoFrame frame = VideoFrame::fromImage(width, height, pixels);
  ASSERT_EQ(frame.width,  width);
  ASSERT_EQ(frame.height, height);

  /* check all pixels */
  for (int i = 0; i < width * height; i++) {
    unsigned r, g, b, a;
    sscanf(colors[i], "#%2x%2x%2x%2x", &r, &g, &b, &a);
    ASSERT_EQ(frame.Y [i], (unsigned char) rgbToY (r, g, b));
    ASSERT_EQ(frame.Cb[i], (unsigned char) rgbToCb(r, g, b));
    ASSERT_EQ(frame.Cr[i], (unsigned char) rgbToCr(r, g, b));
  }
}
