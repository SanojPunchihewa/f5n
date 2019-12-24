# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.10.1)


# Set the path to include directories
include_directories(${CMAKE_SOURCE_DIR})

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp
        )

# add needed libraries
add_library(libf5c SHARED IMPORTED)
#set_target_properties(libf5c PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/armeabi-v7a/libf5cshared.so)
#set_target_properties(libf5c PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a/libf5cshared.so)
set_target_properties(libf5c PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libf5cshared.so)

add_library(libminimap SHARED IMPORTED)
set_target_properties(libminimap PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libminimapshared.so)

add_library(libsamtool SHARED IMPORTED)
#target_compile_options(${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libsamtoolshared.so PUBLIC -fsanitize=address -fno-omit-frame-pointer)
set_target_properties(libsamtool PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libsamtoolshared.so LINK_FLAGS -fsanitize=address)
## add needed libraries
#add_library( libz1 SHARED IMPORTED )
#set_target_properties(libz1 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/armeabi-v7a/libqz.so.1)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries(native-lib libf5c libminimap libsamtool ${log-lib})