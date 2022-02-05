cmake_minimum_required(VERSION 3.1)

embed_files(
  ${CMAKE_CURRENT_BINARY_DIR}/ShaderSources.inl
  NodeShader.vert
  NodeShader.frag
  EdgeShader.vert
  EdgeShader.frag
)

set(SRC_SATVIZ_FILES
  Bindings.cpp
  Display.cpp
  OnscreenDisplay.cpp
  OffscreenDisplay.cpp
  GraphRenderer.cpp
  GlUtils.cpp
  Camera.cpp
  VideoController.cpp
  Graph.cpp
  VideoFrame.cpp TheoraEncoder.cpp)

add_library(
  satviz-consumer-native SHARED
  ${SRC_SATVIZ_FILES}
)

target_include_directories(
  satviz-consumer-native
  PRIVATE ${OGDF_INCLUDE_DIRS}
  PRIVATE ${SFML_INCLUDE_DIRS}
  PRIVATE ${THEORA_INCLUDE_DIRS}
  PRIVATE ${CMAKE_CURRENT_BINARY_DIR}
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
