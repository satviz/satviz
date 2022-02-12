cmake_minimum_required(VERSION 3.1)
project(satviz-consumer-native LANGUAGES CXX C)

enable_testing()

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")

#find_package(OGDF CONFIG)
#if(NOT OGDF_FOUND)
#  message(FATAL_ERROR "No OGDF configuration for CMake has been found.")
#endif()

find_package(SFML CONFIG COMPONENTS window REQUIRED)
if(NOT SFML_FOUND)
  message(FATAL_ERROR "No SFML configuration for CMake has been found.")
endif()

find_package(THEORA)
if(NOT THEORA_FOUND)
    message(FATAL_ERROR "Was not able to locate OGG/Theora development files.")
endif()

include(embed)

include_directories(include)

add_subdirectory(src/glad)
add_subdirectory(src/satviz)
add_subdirectory(src/dummy)
add_subdirectory(test)
