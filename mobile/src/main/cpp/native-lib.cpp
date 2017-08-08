#include <jni.h>
#include <math.h>
#include <string>
#include <vector>
#include <sstream>
#include <iostream>
#include "native-lib.h"
// #include "rep.c"

class native_lib {

public:

    double calculateMag(double x, double y, double z) {

        double sum = pow(x, 2) + pow(y, 2) + pow(z, 2);

        return sqrt(sum);
    }

    /*void call_rep(mpu2 mpu21) {
        _rep_count_data_passing_2(&mpu21, 0, 0, 0, 0);
    }*/

};

native_lib nativeLibObj;

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_nikhil_phone_callnative_CallNativeFunctions_helloWorld(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello World from C++";
    LOGD("LOGD HELLO WORLD");
    LOGE("LOGE ERROR LOG");
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT double JNICALL
Java_com_nikhil_phone_callnative_CallNativeFunctions_calculateMag(
        JNIEnv *env,
        jobject,
        double x, double y, double z) {

    return nativeLibObj.calculateMag(x, y, z);
}

/*JNIEXPORT void JNICALL
Java_com_nikhil_phone_callnative_CallNativeFunctions_calcRep(
        JNIEnv *env,
        jobject,
        double ax, double ay, double az,
        double gx, double gy, double gz) {

    mpu2 mpu21;

    mpu21.x_accel = ax;
    mpu21.y_accel = ay;
    mpu21.z_accel = az;

    mpu21.x_gyro = gx;
    mpu21.y_gyro = gy;
    mpu21.z_gyro = gz;

    nativeLibObj.call_rep(mpu21);

}*/

}