cmake_minimum_required(VERSION 3.22.1)

project("libosmand")

add_library(
    osmand_native
    SHARED
    libosmand.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    osmand_native
    ${log-lib}
)
