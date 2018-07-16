#include <iostream>
#include "io_github_matthewacon_pal_NativeUtils.h"
#include <jvmti.h>
#include <jni.h>
#include <iostream>
#include <fstream>

static const jvmtiCapabilities capabilities = {
 .can_access_local_variables = 1
};

static void throw_jni_exception(JNIEnv *env, const char *clazz, const char *message, long error, const char *err_type) {
 jclass excepClass = env->FindClass(clazz);
 std::string str_msg(message);
 str_msg = (((str_msg + " (") + err_type) + " error code ") + std::to_string(error) + ")";
 if (excepClass == NULL || env->ThrowNew(excepClass, str_msg.c_str()) != 0) {
  throw ("Exception " + std::string(clazz) + " cannot be found!");
 }
}

#define EXCEPTION_ON_JVMTI_ERROR(JVMTI_ERROR, ENV, MESSAGE)\
if (JVMTI_ERROR != 0) throw_jni_exception(ENV, "java/lang/Exception", MESSAGE, JVMTI_ERROR, "jvmti");

#define EXCEPTION_ON_JVM_ERROR(JVM_ERROR, ENV, MESSAGE)\
if (JVM_ERROR != JNI_OK) throw_jni_exception(ENV, "java/lang/Exception", MESSAGE, JVM_ERROR, "jvm");

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
 jvmtiEnv *jvmti;
 jint err = vm->GetEnv((void **)&jvmti, JVMTI_VERSION_1_2);
 if (err != JNI_OK) {
  throw "ERROR GETTING JVMTI INSTANCE!";
 }
// jvmtiCapabilities potential_capabilities;
 jvmtiError error = jvmti->AddCapabilities(&capabilities);
// jvmtiError error = jvmti->GetCapabilities(&potential_capabilities);
 if (error != jvmtiError::JVMTI_ERROR_NONE) {
  throw (std::string("Could not register agent capabilities with target JVM! err: ") + std::to_string(error)).c_str();
 }
 return JNI_VERSION_1_8;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {}

JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM *jvm, char *options, void *reserved) {
 return JNI_OK;
}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *jvm, char *options, void *reserved) {
 jvmtiEnv *jvmti;
 jint error = jvm->GetEnv((void **)&jvmti, JVMTI_VERSION_1_2);
 if (error != 0) {
  throw (std::string("Could not retrieve JVM environment! err: ") + std::to_string(error)).c_str();
 }
 error = jvmti->AddCapabilities(&capabilities);
 if (error != jvmtiError::JVMTI_ERROR_NONE) {
  throw (std::string("Could not register agent capabilities with target JVM! err: ") + std::to_string(error)).c_str();
 }
 return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *jvm) {}

JNIEXPORT jobject JNICALL Java_NativeUtils_getInstanceFromStack(JNIEnv *env, jclass ignored, jint depth) {
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
 jvmtiCapabilities returnedCapabilities;
 jvmti->GetPotentialCapabilities(&returnedCapabilities);
 EXCEPTION_ON_JVMTI_ERROR(
  jvmti->GetLocalInstance(thread, depth, &obj),
  env,
  (*new std::string("Could not fetch object at frame depth ") + std::to_string(depth) + "!").c_str()
 );
 EXCEPTION_ON_JVMTI_ERROR(
  jvmti->RelinquishCapabilities(&capabilities),
  env,
  "WARNING: Error relinquishing registered JVM capabilities!"
 );
 return obj;
}
