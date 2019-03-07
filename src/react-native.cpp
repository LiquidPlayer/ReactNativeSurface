/*
 * Copyright (c) 2019 Eric Lange
 *
 * Distributed under the MIT License.  See LICENSE.md at
 * https://github.com/LiquidPlayer/caraml-console for terms and conditions.
 */
#include "node.h"
#include "v8.h"
#include "react-native.h"

using namespace v8;

void Init(Local<Object> target)
{
}

NODE_MODULE_CONTEXT_AWARE(ReactNative,Init)

extern "C" void register_reactnative()
{
    _register_ReactNative();
}

#ifdef __ANDROID__
#include <jni.h>
extern "C" void JNICALL Java_org_liquidplayer_addon_ReactNative_register(JNIEnv* env, jobject thiz)
{
    register_reactnative();
}
#endif
