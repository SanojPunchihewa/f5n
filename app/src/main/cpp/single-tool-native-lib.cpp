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

#include "tools.h"
#include "interface_artic.h"
#include "interface_bcftools.h"
#include "interface_bioawk.h"

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
Java_com_mobilegenomics_genopo_core_NativeCommands_init(JNIEnv *env, jobject, jstring command, jint command_id) {
  // try{TODO:exceptions
  // Convert command to cpp
  //TODO:casting not good
  char *command_c = (char *) env->GetStringUTFChars(command, nullptr);
  int max_len = strlen(command_c);
  enum { kMaxArgs = 64 };
  int argc = 0;
  char *argv[kMaxArgs];

  char *p2 = strtok(command_c, " ");

  const char search = '\'';

  while (p2 && argc < kMaxArgs-1){ // arguments can be lengthy, quoted ones. Hence, have to do more parsing than in other pipelines.
    // fprintf(stderr,"p = %s\n",p2);
    char * pos1 = strchr(p2,search);
    char * argument_ptr = (char*)malloc(max_len);
    strcpy(argument_ptr, p2);
    int has_quotes = 0;
    if(pos1 != NULL){
        char * pos2 = strchr(pos1+1,search);
        if(pos2 == NULL){
            char * pos3;
            do {
                p2 = strtok(0, " ");
                strcat(argument_ptr, " ");
                strcat(argument_ptr, p2);
                pos3 = strchr(p2,search);
            }while (p2 && pos3 == NULL);
            if(*(pos3+1) != '\0'){
                fprintf(stderr,"%c is not found at the end of an argument",search);
                return 1;
            }
        }
        else if(*(pos2+1) != '\0'){
            fprintf(stderr,"%c is found more than twice in an argument.\n",search);
            return 1;
        }
        // strip ['] from argument
        has_quotes = 1;
        // argument_ptr++;
        argument_ptr[strlen(argument_ptr)-1] = 0;

    }
    if(has_quotes){
        argv[argc++] = ++argument_ptr;
    } else{
        argv[argc++] = argument_ptr;
    }
    p2 = strtok(0, " ");
    }

  argv[argc] = 0;
  jint result = 1;
  char exceptionBuffer[1024];
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
    resetOptInd();

    int tool = command_id / 10;
    switch (tool) {
      case ARTIC:
        sprintf(exceptionBuffer, "ARTIC_EXCEPTION");
        result = init_artic(argc, argv);
        break;
      case BCFTOOLS:
        sprintf(exceptionBuffer, "BCFTOOLS_EXCEPTION");
        result = init_bcftools(argc, argv);
        break;
      case BIOAWK:
        sprintf(exceptionBuffer, "BIOAWK_EXCEPTION");
        result = init_bioawk(argc, argv);
        break;
    }
    ///

  } else {
    resetOptInd();
    (env)->ThrowNew((env)->FindClass("java/lang/Exception"), exceptionBuffer);
  }
  // if everything was OK, we can set old handlers
  sigaction(10, &actions[10 - 1], NULL);
  sigaction(11, &actions[11 - 1], NULL);

  env->ReleaseStringUTFChars(command, command_c);

  // releasing dynamically allocated argvs
  for(int i = 0; i<argc;i++){
    free(argv[i]);
  }
  return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_mobilegenomics_genopo_core_NativeCommands_startPipeline(JNIEnv *env, jobject thiz, jstring pipe_path) {
  outfile = env->GetStringUTFChars(pipe_path, nullptr);

  int out = mkfifo(outfile, 0664);
  fdo = open(outfile, O_WRONLY);

  dup2(fdo, STDERR_FILENO);
  setvbuf(stderr, 0, _IONBF, 0);
  return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_mobilegenomics_genopo_core_NativeCommands_finishPipeline(JNIEnv *env, jobject thiz, jstring pipe_path) {
  fprintf(stderr, FILE_CLOSE_TAG);
  fflush(stderr);
  close(fdo);
  env->ReleaseStringUTFChars(pipe_path, outfile);
  return 0;
}