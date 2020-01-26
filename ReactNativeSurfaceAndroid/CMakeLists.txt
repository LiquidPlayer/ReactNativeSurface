
cmake_minimum_required(VERSION 3.4.1)

add_library(
  react-native.node
  SHARED

  # JavaScriptCore -> V8 bridge
  src/main/cpp/JSCUnimplemented.cpp

  # node addon
  src/../../src/react-native.cpp

  # OnLoad
  src/main/cpp/ReactOnLoad.cpp
  src/main/cpp/fbOnLoad.cpp
  src/main/cpp/OnLoad.cpp
  src/main/cpp/YGJNI.cpp

  # glog
  src/../build/third-party-ndk/glog/glog-0.3.5/src/demangle.cc
  src/../build/third-party-ndk/glog/glog-0.3.5/src/logging.cc
  src/../build/third-party-ndk/glog/glog-0.3.5/src/raw_logging.cc
  src/../build/third-party-ndk/glog/glog-0.3.5/src/signalhandler.cc
  src/../build/third-party-ndk/glog/glog-0.3.5/src/symbolize.cc
  src/../build/third-party-ndk/glog/glog-0.3.5/src/utilities.cc
  src/../build/third-party-ndk/glog/glog-0.3.5/src/vlog_is_on.cc
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fbgloginit/glog_init.cpp

  # folly
  src/../build/third-party-ndk/folly/folly/json.cpp
  src/../build/third-party-ndk/folly/folly/Unicode.cpp
  src/../build/third-party-ndk/folly/folly/Conv.cpp
  src/../build/third-party-ndk/folly/folly/Demangle.cpp
  src/../build/third-party-ndk/folly/folly/detail/MallocImpl.cpp
  src/../build/third-party-ndk/folly/folly/StringBase.cpp
  src/../build/third-party-ndk/folly/folly/dynamic.cpp

  # yoga
  src/../../deps/react-native-0.56.0/ReactCommon/yoga/yoga/Utils.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/yoga/yoga/YGEnums.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/yoga/yoga/YGLayout.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/yoga/yoga/YGNodePrint.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/yoga/yoga/Yoga.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/yoga/yoga/YGConfig.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/yoga/yoga/YGFloatOptional.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/yoga/yoga/YGNode.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/yoga/yoga/YGStyle.cpp

  # fb
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/assert.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/ByteBuffer.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/Countable.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/Environment.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/Exceptions.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/fbjni.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/Hybrid.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/jni_helpers.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/LocalString.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/OnLoad.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/ReadableByteChannel.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/References.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/jni/WeakReference.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/log.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/lyra/lyra.cpp

  # double-conversion
  src/../build/third-party-ndk/double-conversion/double-conversion/bignum.cc
  src/../build/third-party-ndk/double-conversion/double-conversion/bignum-dtoa.cc
  src/../build/third-party-ndk/double-conversion/double-conversion/cached-powers.cc
  src/../build/third-party-ndk/double-conversion/double-conversion/diy-fp.cc
  src/../build/third-party-ndk/double-conversion/double-conversion/double-conversion.cc
  src/../build/third-party-ndk/double-conversion/double-conversion/fast-dtoa.cc
  src/../build/third-party-ndk/double-conversion/double-conversion/fixed-dtoa.cc
  src/../build/third-party-ndk/double-conversion/double-conversion/strtod.cc

  # ReactCommon/cxxreact
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/CxxNativeModule.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/Instance.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSBigString.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSBundleType.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSCExecutor.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSCLegacyTracing.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSCMemory.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSCNativeModules.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSCPerfStats.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSCSamplingProfiler.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSCTracing.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSCUtils.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSDeltaBundleClient.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSExecutor.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/JSIndexedRAMBundle.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/MethodCall.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/ModuleRegistry.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/NativeToJsBridge.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/Platform.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/RAMBundleRegistry.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/cxxreact/ReactMarker.cpp

  # ReactCommon/jschelpers
  src/../../deps/react-native-0.56.0/ReactCommon/jschelpers/JSCHelpers.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/jschelpers/Unicode.cpp
  src/../../deps/react-native-0.56.0/ReactCommon/jschelpers/Value.cpp

  # ReactCommon/privatedata
  src/../../deps/react-native-0.56.0/ReactCommon/privatedata/PrivateDataBase.cpp

  # ReactCommon/jsinspector
  src/../../deps/react-native-0.56.0/ReactCommon/jsinspector/InspectorInterfaces.cpp

  # ReactAndroid/react
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/AndroidJSCFactory.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/CatalystInstanceImpl.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/CxxModuleWrapper.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/JavaModuleWrapper.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/JReactMarker.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/JMessageQueueThread.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/JSCPerfLogging.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/JSLoader.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/JSLogging.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/JniJSModulesUnbundle.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/MethodInvoker.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/ModuleRegistryBuilder.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/NativeArray.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/NativeCommon.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/NativeDeltaClient.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/NativeMap.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/ProxyExecutor.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/ReadableNativeArray.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/ReadableNativeMap.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/WritableNativeArray.cpp
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni/WritableNativeMap.cpp
)

include_directories(
  src/../../deps/react-native-0.56.0
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/third-party/glog
  src/../build/third-party-ndk
  src/../build/third-party-ndk/glog/glog-0.3.5/src
  src/../build/third-party-ndk/folly
  src/../build/third-party-ndk/double-conversion
  src/../build/third-party-ndk/boost/boost_1_63_0
  src/../../deps/react-native-0.56.0/ReactCommon
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fb/include
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/first-party/fbgloginit
  src/../../deps/react-native-0.56.0/ReactAndroid/src/main/jni/react/jni
  src/../../deps/react-native-0.56.0/ReactCommon/yoga

  build/liquidcore-addon/include/node
  build/liquidcore-addon/include/v8
  build/liquidcore-addon/include/
)

unset(DEFS_RELEASE)
unset(CFLAGS_RELEASE)

list (APPEND DEFS_RELEASE
  -D_START_GOOGLE_NAMESPACE_="namespace google {"
  -D_END_GOOGLE_NAMESPACE_="}"
  -DFOLLY_NO_CONFIG=1
  -DFOLLY_HAVE_CLOCK_GETTIME=1
  -DFOLLY_HAVE_MEMRCHR=1
  -DFOLLY_USE_LIBCPP
  -DHAVE_POSIX_CLOCKS
  -DDISABLE_CPUCAP
  -DDISABLE_XPLAT
)

list (APPEND CFLAGS_RELEASE
  -Wall
  -Wwrite-strings
  -Woverloaded-virtual
  -Wno-sign-compare
  -Wno-unused-local-typedefs
  -Wno-unused-variable
  -Wno-unused-lambda-capture
  -Wno-mismatched-tags
  -DNDEBUG
  -g
  -O2
  -std=c++14
  -fexceptions
  -fno-omit-frame-pointer
  -frtti
)

if(${CMAKE_BUILD_TYPE} STREQUAL Debug)
  list (APPEND CFLAGS_RELEASE -DDEBUG)
endif()

string (REPLACE ";" " " CPP_FLAGS_STR "${CFLAGS_RELEASE} ${DEFS_RELEASE}")
SET( CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} ${CPP_FLAGS_STR}" )

find_library( log-lib log )
find_library( android-lib android )

add_library(
  js-lib
  SHARED
  IMPORTED
)
add_library(
  liquidcore-lib
  SHARED
  IMPORTED
)
set_target_properties(
  js-lib
  PROPERTIES IMPORTED_LOCATION
  ${PROJECT_SOURCE_DIR}/build/liquidcore-addon/jni/${ANDROID_ABI}/libliquidjs.so
)
set_target_properties(
  liquidcore-lib
  PROPERTIES IMPORTED_LOCATION
  ${PROJECT_SOURCE_DIR}/build/liquidcore-addon/jni/${ANDROID_ABI}/libliquidnode.so
)

target_link_libraries(
  react-native.node
  ${log-lib}
  ${android-lib}
  js-lib
  liquidcore-lib
)