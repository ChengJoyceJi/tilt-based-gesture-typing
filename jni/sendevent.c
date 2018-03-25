//
// Created by Joyce on 2018-03-22.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/time.h>
//#include <linux/input.h> // this does not compile
#include <errno.h>
#include <unistd.h>
// from <linux/input.h>
struct input_event {
	struct timeval time;
	__u16 type;
	__u16 code;
	__s32 value;
};

// del
#include <jni.h>
#include <android/log.h>

#define DEBUG_TAG "MainActivity"
//

#define EVIOCGVERSION		_IOR('E', 0x01, int)			/* get driver version */
#define EVIOCGID		_IOR('E', 0x02, struct input_id)	/* get device ID */
#define EVIOCGKEYCODE		_IOR('E', 0x04, int[2])			/* get keycode */
#define EVIOCSKEYCODE		_IOW('E', 0x04, int[2])			/* set keycode */
#define EVIOCGNAME(len)		_IOC(_IOC_READ, 'E', 0x06, len)		/* get device name */
#define EVIOCGPHYS(len)		_IOC(_IOC_READ, 'E', 0x07, len)		/* get physical location */
#define EVIOCGUNIQ(len)		_IOC(_IOC_READ, 'E', 0x08, len)		/* get unique identifier */
#define EVIOCGKEY(len)		_IOC(_IOC_READ, 'E', 0x18, len)		/* get global keystate */
#define EVIOCGLED(len)		_IOC(_IOC_READ, 'E', 0x19, len)		/* get all LEDs */
#define EVIOCGSND(len)		_IOC(_IOC_READ, 'E', 0x1a, len)		/* get all sounds status */
#define EVIOCGSW(len)		_IOC(_IOC_READ, 'E', 0x1b, len)		/* get all switch states */
#define EVIOCGBIT(ev,len)	_IOC(_IOC_READ, 'E', 0x20 + ev, len)	/* get event bits */
#define EVIOCGABS(abs)		_IOR('E', 0x40 + abs, struct input_absinfo)		/* get abs value/limits */
#define EVIOCSABS(abs)		_IOW('E', 0xc0 + abs, struct input_absinfo)		/* set abs value/limits */
#define EVIOCSFF		_IOC(_IOC_WRITE, 'E', 0x80, sizeof(struct ff_effect))	/* send a force effect to a force feedback device */
#define EVIOCRMFF		_IOW('E', 0x81, int)			/* Erase a force effect */
#define EVIOCGEFFECTS		_IOR('E', 0x84, int)			/* Report number of effects playable at the same time */
#define EVIOCGRAB		_IOW('E', 0x90, int)			/* Grab/Release device */
// end <linux/input.h>

int Java_com_example_joyce_myapplication_GestureTypingService_openfile(JNIEnv * env, jobject this) {
    int fd ;
    char gpio_path[19];
    sprintf(gpio_path,"/dev/input/event2");

    fd = open(gpio_path, O_RDWR | O_NONBLOCK );

    if(fd < 0) {
        //fprintf(stderr, "could not open, %s\n", strerror(errno));
        __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "could not open fd: [%d]", fd);
        return -1;
    }

    return fd;
}

int Java_com_example_joyce_myapplication_GestureTypingService_closefile(JNIEnv * env, jobject this, jint jfd) {
    int fd = (int) jfd;
    if (fd >= 0) {
        close(fd);
    }
    return 0;
}

int Java_com_example_joyce_myapplication_GestureTypingService_sendevent(JNIEnv * env, jobject this, jint jfd, jstring type, jstring code, jstring value)
{
    jboolean isCopy;

    const char * szLogThis2 = (*env)->GetStringUTFChars(env, type, &isCopy);

    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", szLogThis2);

    const char * szLogThis3 = (*env)->GetStringUTFChars(env, code, &isCopy);

    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", szLogThis3);

    const char * szLogThis4 = (*env)->GetStringUTFChars(env, value, &isCopy);

    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", szLogThis4);


    int fd = (int) jfd;
    ssize_t ret;
    int version;
    struct input_event event;
    //fd = open(szLogThis1, O_RDWR);

    /*char gpio_path[19];
    sprintf(gpio_path,"/dev/input/event2");

    fd = open(gpio_path, O_RDWR | O_NONBLOCK );
    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "FD: [%d]", fd); */

    if(fd < 0) {
        //fprintf(stderr, "could not open, %s\n", strerror(errno));
        char* a = "a";
        __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", a);
        return 1;
    }
    if (ioctl(fd, EVIOCGVERSION, &version)) {
        //fprintf(stderr, "could not get driver version for, %s\n", strerror(errno));
        char* b = "b";
        __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", b);
        return 1;
    }

    char* c = "c";
    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", c);

    memset(&event, 0, sizeof(event));
    event.type = atoi(szLogThis2);
    event.code = atoi(szLogThis3);
    event.value = atoi(szLogThis4);
    ret = write(fd, &event, sizeof(event));
    if(ret < (ssize_t) sizeof(event)) {
        char* d = "d";
        __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", d);
        //fprintf(stderr, "write event failed, %s\n", strerror(errno));
        return -1;
    }

    char* e = "e";
    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", e);

    (*env)->ReleaseStringUTFChars(env, type, szLogThis2);
    (*env)->ReleaseStringUTFChars(env, code, szLogThis3);
    (*env)->ReleaseStringUTFChars(env, value, szLogThis4);
    return 0;
}

void Java_com_example_joyce_myapplication_MainActivity_helloLog(JNIEnv * env, jobject this, jstring logThis)
{
    jboolean isCopy;
    const char * szLogThis = (*env)->GetStringUTFChars(env, logThis, &isCopy);

    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", szLogThis);

    (*env)->ReleaseStringUTFChars(env, logThis, szLogThis);
}