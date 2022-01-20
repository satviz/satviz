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
)

gtest_discover_tests(
  native-test
)