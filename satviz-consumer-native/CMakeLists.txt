cmake_minimum_required(VERSION 3.1)
project(satviz-consumer-native CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

add_library(
    satviz-consumer-native SHARED
    src/Bindings.cpp
)

