LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_LIB_TYPE        := STATIC
OPENCV_INSTALL_MODULES := on
OPENCV_CAMERA_MODULES  := off

include ../../opencv/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := object_tracking
LOCAL_SRC_FILES := obj_tracking.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
