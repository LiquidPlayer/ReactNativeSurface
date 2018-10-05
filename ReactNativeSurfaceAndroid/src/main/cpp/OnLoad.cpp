//
// Created by Eric Lange on 10/5/18.
//

#include <jni.h>

JNIEXPORT jint fbOnLoad_JNI_OnLoad(JavaVM* vm, void* reserved);
extern "C" JNIEXPORT jint React_JNI_OnLoad(JavaVM* vm, void* reserved);

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    fbOnLoad_JNI_OnLoad(vm, reserved);
    return React_JNI_OnLoad(vm, reserved);
}