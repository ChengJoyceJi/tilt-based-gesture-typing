LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog

LOCAL_MODULE    := myapplication
LOCAL_SRC_FILES := sendevent.c

include $(BUILD_SHARED_LIBRARY)