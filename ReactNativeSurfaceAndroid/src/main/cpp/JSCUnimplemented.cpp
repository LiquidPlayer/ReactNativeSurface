//
// Created by Eric Lange on 10/5/18.
//

#include <assert.h>

// Crash the app (with a descriptive stack trace) if a function that is not supported by
// the system JSC is called.
#define UNIMPLEMENTED_SYSTEM_JSC_FUNCTION(FUNC_NAME)            \
extern "C" void FUNC_NAME(void* args...) { \
  assert(false);                                                \
}

UNIMPLEMENTED_SYSTEM_JSC_FUNCTION(JSEvaluateBytecodeBundle)
#if WITH_FBJSCEXTENSIONS
UNIMPLEMENTED_SYSTEM_JSC_FUNCTION(JSStringCreateWithUTF8CStringExpectAscii)
#endif
UNIMPLEMENTED_SYSTEM_JSC_FUNCTION(JSPokeSamplingProfiler)
UNIMPLEMENTED_SYSTEM_JSC_FUNCTION(JSStartSamplingProfilingOnMainJSCThread)

UNIMPLEMENTED_SYSTEM_JSC_FUNCTION(JSGlobalContextEnableDebugger)
UNIMPLEMENTED_SYSTEM_JSC_FUNCTION(JSGlobalContextDisableDebugger)

UNIMPLEMENTED_SYSTEM_JSC_FUNCTION(configureJSCForIOS)

UNIMPLEMENTED_SYSTEM_JSC_FUNCTION(FBJSContextStartGCTimers)

bool JSSamplingProfilerEnabled() {
    return false;
}

