cmake_minimum_required(VERSION 3.22.1)

project("unifiedcast_native")

add_library(
        unifiedcast_native
        SHARED
        native_engine.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        unifiedcast_native
        ${log-lib})
