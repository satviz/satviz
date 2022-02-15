#include <satviz/OffscreenDisplay.hpp>
#include <satviz/YCbCr.hpp>
#include <glad/gl.h>

#include <gtest/gtest.h>

#include <cstdlib>

using namespace ::satviz::video;

static bool isX11Running() {
  char *dpy = getenv("DISPLAY");
  return dpy && dpy[0];
}

/*
 * Test capturing the on-screen frame as a VideoFrame.
 */
TEST(Display, FrameCapture) {
  if (!isX11Running()) {
    GTEST_SKIP();
  }

  OffscreenDisplay dpy(16, 16);
  VideoGeometry geom;
  geom.view_width    = dpy.getWidth();
  geom.view_height   = dpy.getHeight();
  geom.view_offset_x = 0;
  geom.view_offset_y = 0;
  geom.padded_width  = dpy.getWidth();
  geom.padded_height = dpy.getHeight();

  // Draw a completely red frame
  dpy.startFrame();
  glClearColor(1.0, 0.0, 0.0, 1.0);
  glClear(GL_COLOR_BUFFER_BIT);
  dpy.transferCurrentFrame();
  dpy.endFrame();

  // Draw a completely green frame
  dpy.startFrame();
  glClearColor(0.0, 1.0, 0.0, 1.0);
  glClear(GL_COLOR_BUFFER_BIT);
  dpy.transferCurrentFrame();
  VideoFrame frame1 = dpy.grabPreviousFrame(geom); // Grab red frame during green frame
  dpy.endFrame();
  VideoFrame frame2 = dpy.grabPreviousFrame(geom); // Grab green frame once it's available

  // Make sure the red frame actually has red pixels
  ASSERT_EQ(frame1.Y [0], rgbToY (255, 0, 0));
  ASSERT_EQ(frame1.Cb[0], rgbToCb(255, 0, 0));
  ASSERT_EQ(frame1.Cr[0], rgbToCr(255, 0, 0));

  // Make sure the green frame actually has green pixels
  ASSERT_EQ(frame2.Y [0], rgbToY (0, 255, 0));
  ASSERT_EQ(frame2.Cb[0], rgbToCb(0, 255, 0));
  ASSERT_EQ(frame2.Cr[0], rgbToCr(0, 255, 0));
}
