#include <jni.h>
#include <math.h>
#include <string>
#include <vector>
#include <sstream>
#include <iostream>


class native_lib {

public:

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
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT double JNICALL
Java_com_nikhil_phone_callnative_CallNativeFunctions_calculateMag(
        JNIEnv *env,
        jobject,
        double x, double y, double z) {

    return nativeLibObj.calculateMag(x, y, z);
}

}