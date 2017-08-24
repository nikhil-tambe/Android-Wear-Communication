#include <jni.h>
#include <math.h>
#include <string>
#include <vector>
#include <sstream>
#include <iostream>

#include "native-lib.h"

class native_lib {

    std::string s = "hello NDK";

public:

    //**********************************************************************************************
    std::string helloString() {
        return s;
    }

    //**********************************************************************************************
    double calculateMag(double x, double y, double z) {

        double sum = pow(x, 2) + pow(y, 2) + pow(z, 2);

        return sqrt(sum);
    }

};

native_lib nativeLibObj;

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_nikhil_phone_callnative_CallNativeFunctions_helloWorld(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello World from C++";
    LOGD("LOG-D HELLO WORLD");
    LOGI("LOG-I HELLO WORLD");
    LOGE("LOG-E ERROR LOG");
    return env->NewStringUTF(nativeLibObj.helloString().c_str());
}

JNIEXPORT double JNICALL
Java_com_nikhil_phone_callnative_CallNativeFunctions_calculateMag(
        JNIEnv *env,
        jobject,
        double x, double y, double z) {

    return nativeLibObj.calculateMag(x, y, z);
}

}