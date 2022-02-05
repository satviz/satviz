#include <gtest/gtest.h>
#include <satviz/YCbCr.hpp>

using namespace ::satviz::video;

/*
 * Compare the output of the RGB to YCbCr conversion functions to known values.
 */
TEST(YCbCr, KnownValues) {
  // black
  ASSERT_EQ(rgbToY (0, 0, 0),  16);
  ASSERT_EQ(rgbToCb(0, 0, 0), 128);
  ASSERT_EQ(rgbToCr(0, 0, 0), 128);

  // white
  ASSERT_EQ(rgbToY (255, 255, 255), 235);
  ASSERT_EQ(rgbToCb(255, 255, 255), 128);
  ASSERT_EQ(rgbToCr(255, 255, 255), 128);

  // yellow
  ASSERT_EQ(rgbToY (255, 255, 0), 210);
  ASSERT_EQ(rgbToCb(255, 255, 0),  16);
  ASSERT_EQ(rgbToCr(255, 255, 0), 145);
}
