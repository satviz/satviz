// This file only exists to test out Google Test / CMake / CTest / Github Actions interoperability.
#include <gtest/gtest.h>

// Dummy Test just to check if testing system works
TEST(HelloTest, BasicAssertions) {
  EXPECT_STRNE("hello", "world");
  EXPECT_EQ(7 * 6, 42);
}
