#include <iostream>
#include <fstream>
#include <jvmti.h>
#include <jni.h>

#include "exception_utils.h"

static const jvmtiCapabilities capabilities = {
 .can_access_local_variables = 1
};

static bool initialized = false;

static void init(JavaVM *vm) {
 if (!initialized) {
  initialized = true;
  jvmtiEnv *jvmti;
  jint err = vm->GetEnv((void **) &jvmti, JVMTI_VERSION_1_2);
  if (err != JNI_OK) {
   throw std::runtime_error(
    std::string("Could not retrieve JVM environment! err: ") +
    std::to_string(err)
   );
  }
  jvmtiError error = jvmti->AddCapabilities(&capabilities);
  if (error != jvmtiError::JVMTI_ERROR_NONE) {
   throw std::runtime_error(
    std::string("Could not register agent capabilities with target JVM! err: ") +
    std::to_string(error)
   );
  }
 }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
 init(vm);
 return JNI_VERSION_1_8;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {}

JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM *jvm, char *options, void *reserved) {
 return JNI_OK;
}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *jvm, char *options, void *reserved) {
 init(jvm);
 return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *jvm) {
// jvmtiEnv *jvmti;
// jint err = jvm->GetEnv((void **)&jvmti, JVMTI_VERSION_1_2);
// if (err != JNI_OK) {
//  throw std::runtime_error(
//   std::string("Could not retrieve JVM environment! err: ") +
//   std::to_string(err)
//  );
// }
// jvmtiError error = jvmti->RelinquishCapabilities(&capabilities);
// if (error != jvmtiError::JVMTI_ERROR_NONE) {
//  throw std::runtime_error(
//   std::string("Error relinquishing registered JVM capabilities! err: ") +
//   std::to_string(error)
//  );
// }
}
