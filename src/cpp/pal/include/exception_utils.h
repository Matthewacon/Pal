#pragma once

#ifndef PAL_EXCEPTION_UTILS_H
#define PAL_EXCEPTION_UTILS_H

#include <jni.h>
#include <string>

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

#endif //PAL_EXCEPTION_UTILS_H
