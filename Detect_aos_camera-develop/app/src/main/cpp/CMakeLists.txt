# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--exclude-libs,libippicv.a -Wl,--exclude-libs,libippiw.a")

add_library( # Sets the name of the library.
        native-lib
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        native-lib.cpp
        detect_image_quality.cpp
        detect_verification_mobile.cpp
        detect_nostrildetection.cpp
        )
include_directories(src/main/cpp/include/)
# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log
        )
# opencv 추가 부분
add_library(
        lib-opencv
        SHARED
        IMPORTED
)

set_target_properties(
        lib-opencv
        PROPERTIES
        IMPORTED_LOCATION
        #/Users/dk/Documents/OpenCV-android-sdk/sdk/native/libs/${ANDROID_ABI}/libopencv_java3.so
        #C:/Users/fitpet/Documents/GitHub/Detect_aos_camera/app/src/main/JniLibs/${ANDROID_ABI}/libopencv_java3.so
        /Users/kaco/Documents/Fitpet/Detect/AndroidProj/Detect_aos_camera/app/src/main/JniLibs/${ANDROID_ABI}/libopencv_java3.so
)
# include_directories(/Users/dk/Documents/OpenCV-android-sdk/sdk/native/jni/include)
#include_directories(D:/opencv/opencv_build/install/include)
include_directories(/Users/kaco/Documents/Fitpet/Detect/AndroidProj/opencv_build/install/include)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib
        lib-opencv
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        )
