cmake_minimum_required(VERSION 3.1)

set(SRC_SATVIZ_FILES
  Bindings.cpp
  Display.cpp
  OnscreenDisplay.cpp
  OffscreenDisplay.cpp
)

add_library(
  satviz-consumer-native SHARED
  ${SRC_SATVIZ_FILES}
)

target_include_directories(
  satviz-consumer-native
  PRIVATE ${OGDF_INCLUDE_DIRS}
  PRIVATE ${SFML_INCLUDE_DIRS}
  PRIVATE ${THEORA_INCLUDE_DIRS}
)

target_link_libraries(
  satviz-consumer-native
  PRIVATE OGDF
  PRIVATE sfml-window
  PRIVATE ${THEORA_LIBRARIES}
  PRIVATE satviz-glad
)

target_compile_options(
  satviz-consumer-native
  PRIVATE -Wall -Wextra -Wconversion -pedantic
)
