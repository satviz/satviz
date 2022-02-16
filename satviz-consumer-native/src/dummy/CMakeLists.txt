cmake_minimum_required(VERSION 3.1)

add_executable(
  dummy
  Main.cpp
)

target_link_libraries(
  dummy
  PRIVATE satviz-consumer-native
)

target_compile_options(dummy PRIVATE ${SATVIZ_COMPILER_WARNINGS})
