# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.10.1)


# Set the path to include directoriesf5c
include_directories(${CMAKE_SOURCE_DIR})

add_library( # Sets the name of the library.
        methylation-native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        methylation-native-lib.cpp
        )

add_library( # Sets the name of the library.
        variant-native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        variant-native-lib.cpp
        )

# add needed libraries
add_library(libnanopolish SHARED IMPORTED)
set_target_properties(libnanopolish PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libnanopolish.so)

add_library(libf5c SHARED IMPORTED)
set_target_properties(libf5c PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libf5cshared.so)

add_library(libminimap SHARED IMPORTED)
set_target_properties(libminimap PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libminimapshared.so)

add_library(libsamtool SHARED IMPORTED)
set_target_properties(libsamtool PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libsamtoolshared.so)

add_library(libartic SHARED IMPORTED)
set_target_properties(libartic PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libartic_c.so)

add_library(libhts SHARED IMPORTED)
set_target_properties(libhts PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libhts.so)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library(log-lib log)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries(methylation-native-lib libminimap libsamtool libf5c libhts ${log-lib})

target_link_libraries(variant-native-lib libminimap libsamtool libnanopolish libartic libhts ${log-lib})