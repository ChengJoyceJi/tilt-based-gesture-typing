//
// Created by Joyce on 2018-03-22.
//

#include <jni.h>
#include <string.h>
#include <android/log.h>

#define DEBUG_TAG "MainActivity"

void Java_com_example_joyce_myapplication_MainActivity_helloLog(JNIEnv * env, jobject this, jstring logThis)
{
    jboolean isCopy;
    const char * szLogThis = (*env)->GetStringUTFChars(env, logThis, &isCopy);

    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", szLogThis);

    (*env)->ReleaseStringUTFChars(env, logThis, szLogThis);
}