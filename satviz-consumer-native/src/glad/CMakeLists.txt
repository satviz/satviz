cmake_minimum_required(VERSION 3.1)

add_library(
  satviz-glad STATIC
  gl.c
)

target_compile_options(
  satviz-glad
  PRIVATE -fPIC
)
