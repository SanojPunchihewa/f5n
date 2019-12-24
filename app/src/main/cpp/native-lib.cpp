#include <jni.h>
#include <string>
#include <zlib.h>
#include <stdio.h>
#include <iostream>
#include <setjmp.h>
#include <signal.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include<android/log.h>
#define FILE_CLOSE_TAG "EOF"
const char *outfile;
int fdo;
#include "interface_minimap.h"
#include "interface_f5c.h"
#include "interface_samtool.h"
jmp_buf jmpBuf;
// this is the handler for the risky code
// if we reach here, it means somebody
// tried to call exit
void stopExit() {
  siglongjmp(jmpBuf, 1);
}
//http://jnicookbook.owsiak.org/recipe-No-015/
//handling SIGSEGV/SIGBUS in JNI code (stop JVM from crashing)
// OS X uses SIGBUS in case of accessing incorrect memory region
// Linux will use SIGSEGV - this is why we should use two handlers
// there are 31 possible signals we can handle
struct sigaction actions[31];
// this function will set the handler for a signal
void setup_signal_handler(int sig, void (*handler)(int), struct sigaction *old) {
  struct sigaction action;
  // fill action structure
  // pointer to function that will handle signal
  action.sa_handler = handler;
  // for the masks description take a look
  // at "man sigaction"
  sigemptyset(&(action.sa_mask));
  sigaddset(&(action.sa_mask), sig);
  // you can bring back original signal using
  // SA_RESETHAND passed inside sa_flags
  action.sa_flags = 0;
  // and set new handler for signal
  sigaction(sig, &action, &actions[sig - 1]);
}
// this function will be called whenever signal occurs
void handler(int handle) {
  // be very condense here
  // just do essential stuff and get
  // back to the place you want to be
  write(STDOUT_FILENO, "Hello from handler\n", strlen("Hello from handler\n"));
  // set original signal handler
  sigaction(handle, &actions[handle - 1], NULL);
  // and jump to where we have set long jump
  siglongjmp(jmpBuf, 1);
}
// A program that scans multiple argument vectors, or rescans the same vector more than once,
// and wants to make use of GNU extensions such as '+' and '-' at the start of optstring,
// or changes the value of POSIXLY_CORRECT between scans, must reinitialize getopt_long() by resetting optind to 1
// Doc https://linux.die.net/man/3/optind
void resetOptInd() {
  optind = 1;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_mobilegenomics_f5n_core_NativeCommands_init(JNIEnv *env, jobject, jstring command) {
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
  jint result;
  // http://jnicookbook.owsiak.org/recipe-no-016/
  atexit(stopExit);
  // setup signal handlers
  // signals are counted from 1 - 31. Array indexes are
  // counted from 0 - 30. This is why we do 10-1 and 11-1
  setup_signal_handler(10, handler, &actions[10 - 1]);
  setup_signal_handler(11, handler, &actions[11 - 1]);
  // set the long jump for the signal handler
  // if handler is called it will jump
  // here with the error code specified
  // as parameter of siglongjmp
  // first call to sigsetjmp returns 0
  if (sigsetjmp(jmpBuf, 1) == 0) {
    result = init(argc, argv);
  } else {
    resetOptInd();
    char exceptionBuffer[1024];
    sprintf(exceptionBuffer, "F5C_EXCEPTION");
    (env)->ThrowNew((env)->FindClass("java/lang/Exception"), exceptionBuffer);
  }
  // if everything was OK, we can set old handlers
  sigaction(10, &actions[10 - 1], NULL);
  sigaction(11, &actions[11 - 1], NULL);
  env->ReleaseStringUTFChars(command, command_c);
  return result;
}
static int pfd[2];
static pthread_t thr;
static const char *tag = "myapp";
static void *thread_func(void *) {
  ssize_t rdsz;
  char buf[128];
  while ((rdsz = read(pfd[0], buf, sizeof buf - 1)) > 0) {
    if (buf[rdsz - 1] == '\n') --rdsz;
    buf[rdsz] = 0;  /* add null-terminator */
    __android_log_write(ANDROID_LOG_ERROR, tag, buf);
  }
  return 0;
}
int start_logger(const char *app_name) {
  tag = app_name;
  /* make stdout line-buffered and stderr unbuffered */
  setvbuf(stdout, 0, _IOLBF, 0);
  setvbuf(stderr, 0, _IONBF, 0);
  /* create the pipe and redirect stdout and stderr */
  pipe(pfd);
  dup2(pfd[1], 1);
  dup2(pfd[1], 2);
  /* spawn the logging thread */
  if (pthread_create(&thr, 0, thread_func, 0) == -1)
    return -1;
  pthread_detach(thr);
  return 0;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_mobilegenomics_f5n_core_NativeCommands_initminimap2(JNIEnv *env, jobject type, jstring command) {
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
  jint result;
  // http://jnicookbook.owsiak.org/recipe-no-016/
  atexit(stopExit);
  // setup signal handlers
  // signals are counted from 1 - 31. Array indexes are
  // counted from 0 - 30. This is why we do 10-1 and 11-1
  setup_signal_handler(10, handler, &actions[10 - 1]);
  setup_signal_handler(11, handler, &actions[11 - 1]);
  // set the long jump for the signal handler
  // if handler is called it will jump
  // here with the error code specified
  // as parameter of siglongjmp
  // first call to sigsetjmp returns 0
  if (sigsetjmp(jmpBuf, 1) == 0) {
    result = init_minimap2(argc, argv);
  } else {
    resetOptInd();
    char exceptionBuffer[1024];
    sprintf(exceptionBuffer, "MINIMAP2_EXCEPTION");
    (env)->ThrowNew((env)->FindClass("java/lang/Exception"), exceptionBuffer);
  }
  // if everything was OK, we can set old handlers
  sigaction(10, &actions[10 - 1], NULL);
  sigaction(11, &actions[11 - 1], NULL);
  env->ReleaseStringUTFChars(command, command_c);
  return result;
}
extern "C" JNIEXPORT jint JNICALL
Java_com_mobilegenomics_f5n_core_NativeCommands_initsamtool(JNIEnv *env, jobject clazz, jstring command) {
  start_logger("f5n");
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
  jint result;
  // http://jnicookbook.owsiak.org/recipe-no-016/
  atexit(stopExit);
  // setup signal handlers
  // signals are counted from 1 - 31. Array indexes are
  // counted from 0 - 30. This is why we do 10-1 and 11-1
  setup_signal_handler(10, handler, &actions[10 - 1]);
  setup_signal_handler(11, handler, &actions[11 - 1]);
  // set the long jump for the signal handler
  // if handler is called it will jump
  // here with the error code specified
  // as parameter of siglongjmp
  // first call to sigsetjmp returns 0
  if (sigsetjmp(jmpBuf, 1) == 0) {
    result = init_samtools(argc, argv);
  } else {
    resetOptInd();
    char exceptionBuffer[1024];
    sprintf(exceptionBuffer, "SAMTOOL_EXCEPTION");
    (env)->ThrowNew((env)->FindClass("java/lang/Exception"), exceptionBuffer);
  }
  // if everything was OK, we can set old handlers
  sigaction(10, &actions[10 - 1], NULL);
  sigaction(11, &actions[11 - 1], NULL);
  env->ReleaseStringUTFChars(command, command_c);
  return result;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_mobilegenomics_f5n_core_NativeCommands_startPipeline(JNIEnv *env, jobject thiz, jstring pipe_path) {
  outfile = env->GetStringUTFChars(pipe_path, nullptr);
  int out = mkfifo(outfile, 0664);
  fdo = open(outfile, O_WRONLY);
  dup2(fdo, STDERR_FILENO);
  setvbuf(stderr, 0, _IONBF, 0);
  return 0;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_mobilegenomics_f5n_core_NativeCommands_finishPipeline(JNIEnv *env, jobject thiz, jstring pipe_path) {
  fprintf(stderr, FILE_CLOSE_TAG);
  fflush(stderr);
  close(fdo);
  env->ReleaseStringUTFChars(pipe_path, outfile);
  return 0;
}
