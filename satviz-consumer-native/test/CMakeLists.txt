cmake_minimum_required(VERSION 3.1)

enable_testing()

find_package(GTest REQUIRED)
include(GoogleTest)

add_executable(
  native-test
  YCbCr.cpp
  VideoFrame.cpp
  Display.cpp
  Graph.cpp
  GraphContraction.cpp
)

target_compile_options(native-test PRIVATE ${SATVIZ_COMPILER_WARNINGS})

target_link_libraries(
  native-test
  gtest gtest_main pthread
  satviz-consumer-native
)

gtest_discover_tests(
  native-test
)
