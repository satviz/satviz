cmake_minimum_required(VERSION 3.1)
project(satviz-consumer-native CXX)

set(CMAKE_MODULE_PATH cmake/)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

find_package(OGDF CONFIG)
if(NOT OGDF_FOUND)
  message(FATAL_ERROR "No OGDF configuration for CMake has been found.")
endif()

find_package(SFML CONFIG COMPONENTS window REQUIRED)
if(NOT SFML_FOUND)
  message(FATAL_ERROR "No SFML configuration for CMake has been found.")
endif()

add_library(
  satviz-consumer-native SHARED
  src/Bindings.cpp
)

target_include_directories(
  satviz-consumer-native
  PRIVATE "${OGDF_INCLUDE_DIRS}"
  PRIVATE "${SFML_INCLUDE_DIRS}"
)

target_link_libraries(
  satviz-consumer-native
  PRIVATE OGDF
  PRIVATE sfml-window
)

