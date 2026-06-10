cmake_minimum_required(VERSION 3.22.1)
project(llama-android C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Configure 16 KB page size alignment for Android 15+ / API 35+ / API 37 Compatibility
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

# Download llama.cpp using FetchContent
include(FetchContent)

message(STATUS "Fetching llama.cpp source code from GitHub...")
FetchContent_Declare(
    llama_cpp
    GIT_REPOSITORY https://github.com/ggerganov/llama.cpp.git
    GIT_TAG        b9590
)

set(LLAMA_BUILD_COMMON ON CACHE INTERNAL "Build common")
set(LLAMA_BUILD_TOOLS ON CACHE INTERNAL "Build tools")
set(LLAMA_BUILD_TESTS OFF CACHE INTERNAL "Build tests")
set(LLAMA_BUILD_EXAMPLES OFF CACHE INTERNAL "Build examples")
set(LLAMA_BUILD_SERVER OFF CACHE INTERNAL "Build server")
set(GGML_OPENMP OFF CACHE INTERNAL "Enable OpenMP")
set(GGML_VULKAN OFF CACHE INTERNAL "Enable Vulkan")
set(MTMD_VIDEO OFF CACHE INTERNAL "Enable video support in mtmd")

FetchContent_MakeAvailable(llama_cpp)

# Define our JNI library
add_library(llama-android SHARED 
    llama-jni.cpp
)

# Include directories of the fetched llama.cpp
# Note: FetchContent_MakeAvailable exposes llama_cpp_SOURCE_DIR
target_include_directories(llama-android PRIVATE
    ${llama_cpp_SOURCE_DIR}
    ${llama_cpp_SOURCE_DIR}/src
    ${llama_cpp_SOURCE_DIR}/include
    ${llama_cpp_SOURCE_DIR}/common
    ${llama_cpp_SOURCE_DIR}/examples
    ${llama_cpp_SOURCE_DIR}/tools/mtmd
    ${llama_cpp_SOURCE_DIR}/tools/mtmd/models
)

# Link JNI library with llama
target_link_libraries(llama-android
    mtmd
    llama
    log
    android
)
