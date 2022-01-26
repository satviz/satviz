cmake_minimum_required(VERSION 3.1)

add_executable(
  dummy
  Main.cpp
)

target_include_directories(
  dummy
  PRIVATE ${OGDF_INCLUDE_DIRS}
  PRIVATE ${SFML_INCLUDE_DIRS}
  PRIVATE ${THEORA_INCLUDE_DIRS}
)

target_link_libraries(
  dummy
  PRIVATE satviz-consumer-native
)

target_compile_options(
  dummy
  PRIVATE -Wall -Wextra -Wconversion -pedantic
)
