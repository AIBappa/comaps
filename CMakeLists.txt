cmake_minimum_required(VERSION 3.22.1)
project(omim C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_C_STANDARD 17)
# Our code does not rely on gnu extensions.
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_C_EXTENSIONS OFF)

set(CMAKE_C_VISIBILITY_PRESET hidden)
set(CMAKE_CXX_VISIBILITY_PRESET hidden)
set(CMAKE_VISIBILITY_INLINES_HIDDEN ON)

# Disable build-id generation to make builds reproducible.
add_link_options("LINKER:--build-id=none")

# Fixes warning ld: warning: ignoring duplicate libraries.
if (POLICY CMP0156)
  cmake_policy(SET CMP0156 NEW)
endif()

set(OMIM_ROOT ${CMAKE_SOURCE_DIR})
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${OMIM_ROOT}/cmake")

set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR})

include(OmimPlatform)
include(OmimOptions)
include(OmimConfig)
include(OmimHelpers)
include(OmimTesting)

message(STATUS "Using compiler ${CMAKE_CXX_COMPILER_ID} ${CMAKE_CXX_COMPILER_VERSION}")

if (CMAKE_UNITY_BUILD)
  message(STATUS "Using Unity Build with batch ${CMAKE_UNITY_BUILD_BATCH_SIZE}, use -DCMAKE_UNITY_BUILD=OFF to disable it.")
endif()

if (USE_CCACHE)
  find_program(CCACHE_PROGRAM ccache HINTS /usr/local/bin/)
  if (CCACHE_PROGRAM)
    message(STATUS "Using ccache, use -DUSE_CCACHE=OFF to disable it.")
    set_property(GLOBAL PROPERTY RULE_LAUNCH_COMPILE "${CCACHE_PROGRAM}")
    set_property(GLOBAL PROPERTY RULE_LAUNCH_LINK "${CCACHE_PROGRAM}")
    set(CMAKE_C_COMPILER_LAUNCHER   "${CCACHE_PROGRAM}")
    set(CMAKE_CXX_COMPILER_LAUNCHER "${CCACHE_PROGRAM}")
  endif()
endif()

# Global compile options for all configurations.
add_compile_options(-ffast-math)

# Built-in CMake configurations: Debug, Release, RelWithDebInfo, MinSizeRel
if (${CMAKE_BUILD_TYPE} STREQUAL "Debug")
  add_definitions(-DDEBUG)
  add_compile_options(-fno-omit-frame-pointer)
elseif (${CMAKE_BUILD_TYPE} MATCHES "Rel")
  add_definitions(-DRELEASE)
  add_compile_options(-Ofast $<$<CXX_COMPILER_ID:GNU>:-flto=auto>)  # Also enables -ffast-math
else()
  message(FATAL_ERROR "Unknown build type: " ${CMAKE_BUILD_TYPE})
endif()

if (${CMAKE_BUILD_TYPE} STREQUAL "RelWithDebInfo")
  add_compile_options(-fno-omit-frame-pointer)
endif()

# Linux GCC LTO plugin fix.
if (CMAKE_CXX_COMPILER_ID STREQUAL "GNU" AND CMAKE_BUILD_TYPE MATCHES "^Rel")
  # To force errors if LTO was not enabled.
  add_compile_options(-fno-fat-lto-objects)
  # To fix ar and ranlib "plugin needed to handle lto object".
  string(REGEX MATCH "[0-9]+" GCC_MAJOR_VERSION ${CMAKE_CXX_COMPILER_VERSION})
  file(GLOB_RECURSE plugin /usr/lib/gcc/*/${GCC_MAJOR_VERSION}/liblto_plugin.so)
  set(CMAKE_C_ARCHIVE_CREATE "<CMAKE_AR> --plugin ${plugin} qcs <TARGET> <OBJECTS>")
  set(CMAKE_C_ARCHIVE_FINISH "<CMAKE_RANLIB> --plugin ${plugin} <TARGET>")
  set(CMAKE_CXX_ARCHIVE_CREATE "<CMAKE_AR> --plugin ${plugin} qcs <TARGET> <OBJECTS>")
  set(CMAKE_CXX_ARCHIVE_FINISH "<CMAKE_RANLIB> --plugin ${plugin} <TARGET>")
endif()

message(STATUS "Build type: " ${CMAKE_BUILD_TYPE})

find_program(LLD_FOUND ld.lld)
if (LLD_FOUND)
  message(STATUS "Using ld.lld linker")
  set(CMAKE_EXE_LINKER_FLAGS "-fuse-ld=lld")
else()
  find_program(GOLD_FOUND ld.gold)
  if (GOLD_FOUND)
    message(STATUS "Using ld.gold")
    set(CMAKE_EXE_LINKER_FLAGS "-fuse-ld=gold")
  endif()
endif()

if (NOT SKIP_TESTS)
  enable_testing()
  # Enables ctest -T memcheck with valgrind
  include(CTest)
  if (COVERAGE_REPORT)
    include(OmimCoverage)
  endif ()
endif()

if (NOT PYTHON_VERSION)
  # FDroid build server has 3.5.3 Python installed.
  set(PYTHON_VERSION 3.5)
endif()

# End of setting environment variables

find_package(Threads REQUIRED)

find_package(Threads REQUIRED)

# To allow #include "base/file_name.hpp" in all sources.
include_directories(${CMAKE_HOME_DIRECTORY})

find_package(ZLIB REQUIRED)

# Include 3party dependencies.
add_subdirectory(3party)

## Cross-platform core modules for mobile
add_subdirectory(base)
add_subdirectory(coding)
add_subdirectory(descriptions)
add_subdirectory(drape)
add_subdirectory(drape_frontend)
add_subdirectory(editor)
add_subdirectory(ge0)
add_subdirectory(generator/mwm_diff)
add_subdirectory(geometry)
add_subdirectory(indexer)
add_subdirectory(kml)
add_subdirectory(map)
add_subdirectory(cppjansson)
add_subdirectory(platform)
add_subdirectory(routing)
add_subdirectory(routing_common)
add_subdirectory(search)
add_subdirectory(shaders)
add_subdirectory(storage)
add_subdirectory(tracking)
add_subdirectory(traffic)
add_subdirectory(transit)

## Android-specific native module
if (PLATFORM_ANDROID)
  add_subdirectory(android/sdk/src/main/cpp)
endif()
