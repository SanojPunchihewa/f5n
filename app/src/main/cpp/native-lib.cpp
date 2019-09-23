#include <jni.h>
#include <string>
#include "interface_minimap.h"
#include "interface_f5c.h"
#include "interface_samtool.h"
#include <zlib.h>
#include <stdio.h>
#include <iostream>

extern "C" JNIEXPORT jint JNICALL
Java_com_mobilegenomics_f5n_NativeCommands_init(JNIEnv *env, jobject, jstring command) {
  // try{TODO:exceptions
  // Convert command to cpp
  //TODO:casting not good
  char *command_c = (char *) env->GetStringUTFChars(command, nullptr);
  // if(!command_c) {throwJavaError(env, "jvm could not allocate memory");return;};
  // std::string command_s = command_c;


  enum { kMaxArgs = 64 };
  int argc = 0;
  char *argv[kMaxArgs];

  char *p2 = strtok(command_c, " ");

  while (p2 && argc < kMaxArgs - 1) {
    argv[argc++] = p2;
    p2 = strtok(0, " ");
  }
  argv[argc] = 0;
  jint result = init(argc, argv);

  env->ReleaseStringUTFChars(command, command_c);

  return result;
//    return 100;
  // } CATCH_AND_RETHROW;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_mobilegenomics_f5n_NativeCommands_initminimap2(JNIEnv *env, jobject type, jstring command) {
  // try{TODO:exceptions
  // Convert command to cpp
  //TODO:casting not good
  char *command_c = (char *) env->GetStringUTFChars(command, nullptr);

  enum { kMaxArgs = 64 };
  int argc = 0;
  char *argv[kMaxArgs];

  char *p2 = strtok(command_c, " ");

  while (p2 && argc < kMaxArgs - 1) {
    argv[argc++] = p2;
    p2 = strtok(0, " ");
  }
  argv[argc] = 0;
  jint result = init_minimap2(argc, argv);

  env->ReleaseStringUTFChars(command, command_c);

  return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_mobilegenomics_f5n_NativeCommands_initsamtool(JNIEnv *env, jobject clazz, jstring command) {
  char *command_c = (char *) env->GetStringUTFChars(command, nullptr);

  enum { kMaxArgs = 64 };
  int argc = 0;
  char *argv[kMaxArgs];

  char *p2 = strtok(command_c, " ");

  while (p2 && argc < kMaxArgs - 1) {
    argv[argc++] = p2;
    p2 = strtok(0, " ");
  }
  argv[argc] = 0;
  jint result = init_samtools(argc, argv);

  env->ReleaseStringUTFChars(command, command_c);

  return result;
}