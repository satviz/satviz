cmake_minimum_required(VERSION 3.1)
project(satviz)

enable_testing()

add_subdirectory(satviz-consumer-native)

option(BUILD_SHARED_LIBS "" ON)
option(CMAKE_BUILD_TYPE  "" Release)
add_subdirectory(ogdf ${CMAKE_SOURCE_DIR}/.ogdf-build)
