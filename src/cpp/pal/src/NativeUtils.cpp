#include <jvmti.h>
#include <jni.h>

#include "io_github_matthewacon_pal_NativeUtils.h"
#include "exception_utils.h"
#include "common.h"

JNIEXPORT jobject JNICALL Java_io_github_matthewacon_pal_NativeUtils_getInstanceFromStack(JNIEnv *env, jclass ignored, jint depth) {
 JavaVM *vm;
 EXCEPTION_ON_JVM_ERROR(
  env->GetJavaVM(&vm),
  env,
  "Could not get Virtual Machine instance!"
 );
 jvmtiEnv *jvmti;
 EXCEPTION_ON_JVM_ERROR(
  vm->GetEnv((void **)&jvmti, JVMTI_VERSION_1_2),
  env,
  "Could not get VM environment!"
 );
 jthread thread;
 EXCEPTION_ON_JVMTI_ERROR(
  jvmti->GetCurrentThread(&thread),
  env,
  "Error retrieving the invoking thread!"
 );
 jobject obj;
 EXCEPTION_ON_JVMTI_ERROR(
  jvmti->AddCapabilities(&capabilities),
  env,
  "Could not register JNI capabilities!"
 );
// logFile << "can_access_local_variables = " << std::to_string(returnedCapabilities.can_access_local_variables);
 EXCEPTION_ON_JVMTI_ERROR(
  jvmti->GetLocalInstance(thread, depth, &obj),
  env,
  (std::string("Could not fetch object at frame depth ") + std::to_string(depth) + "!").c_str()
 );
 return obj;
}
