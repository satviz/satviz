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
  VideoFrame.cpp
  TheoraEncoder.cpp
  GraphContraction.cpp
)

add_library(
  satviz-consumer-native SHARED
  ${SRC_SATVIZ_FILES}
)

target_compile_options(satviz-consumer-native PRIVATE ${SATVIZ_COMPILER_WARNINGS})

target_include_directories(
  satviz-consumer-native
  PRIVATE ${CMAKE_CURRENT_BINARY_DIR}
)

target_include_directories(
  satviz-consumer-native SYSTEM
  PUBLIC ${CMAKE_SOURCE_DIR}/ogdf/include
  PUBLIC ${SFML_INCLUDE_DIRS}
  PRIVATE ${THEORA_INCLUDE_DIRS}
)

target_link_libraries(
  satviz-consumer-native
  PUBLIC OGDF
  PUBLIC sfml-window
  PRIVATE ${THEORA_LIBRARIES}
  PRIVATE satviz-glad
)
