cmake_minimum_required(VERSION 3.1)

enable_testing()

find_package(GTest REQUIRED)
include(GoogleTest)

add_executable(
  native-test
  Test.cpp
)

target_link_libraries(
  native-test
  gtest gtest_main
  OGDF
  sfml-window
  ${THEORA_LIBRARIES}
)

target_include_directories(
  native-test
  PRIVATE ${OGDF_INCLUDE_DIRS}
  PRIVATE ${SFML_INCLUDE_DIRS}
  PRIVATE ${THEORA_INCLUDE_DIRS}
)

target_compile_options(
  native-test
  PRIVATE -Wall -Wextra -Wconversion -pedantic
)

gtest_discover_tests(
  native-test
)
