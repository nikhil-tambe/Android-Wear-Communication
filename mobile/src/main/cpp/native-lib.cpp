#include <jni.h>
#include <math.h>
#include <string>
#include <vector>
#include <sstream>
#include <iostream>
#include "stdlib.h"

#include "native-lib.h"
// #include "rep.h"
// #include "rep.c"

// 15:17:43 -> 15:18:04 = 21sec  frames=> 516.

class native_lib {

    int stepflag_rep, frameCount;
    int rep_count_var, cycle_count_rep, avglen_rep, tot_samples_rep, oldAccMag, newAccMag,
            avgthresh_rep, newmax_rep, newmin_rep, step_cal_incre_rep,
            count_idle_rep, maxavg_rep, minavg_rep; //, avg_rssdat_rep;
    int accDataArray[WINDOW_SIZE];
    float tot_force, tot_velo, power;
    std::string s = "hello";

public:

    //**********************************************************************************************
    std::string helloString() {
        //LOGD("rep_count_var %f", rep_count_var);
        //LOGD("frameCount %i", frameCount++);
        stepflag_rep = 2;
        avglen_rep = 5;
        return s;
    }

    //**********************************************************************************************
    double calculateMag(double x, double y, double z) {

        double sum = pow(x, 2) + pow(y, 2) + pow(z, 2);

        return sqrt(sum);
    }

    //**********************************************************************************************
    int call_rep(mpu2 *mpu_data_r) {

        float acc_mag, gyro_mag, Ax, Ay, Az, Gx, Gy, Gz;
                //, Ay_rep, Ax_rep, Az_rep, Gx_rep, Gy_rep, Gz_rep;

        Ax = (mpu_data_r->x_accel);
        Ay = (mpu_data_r->y_accel);
        Az = (mpu_data_r->z_accel);

        acc_mag = sqrt((Ax * Ax) + (Ay * Ay) + (Az * Az));

        // avg_rssdat_rep = acc_mag;

        // Gx = (mpu_data_r->x_gyro);
        // Gy = (mpu_data_r->y_gyro);
        // Gz = (mpu_data_r->z_gyro);
        // gyro_mag = sqrt((Gx * Gx) + (Gy * Gy) + (Gz * Gz));

        if (cycle_count_rep >= WINDOW_SIZE) {
            for (int i = 0; i < avglen_rep; i++) {
                accDataArray[i] = accDataArray[cycle_count_rep + i - (avglen_rep)];
            }
            cycle_count_rep = avglen_rep;
        }

        if (tot_samples_rep >= WINDOW_SIZE) {
            tot_samples_rep = avglen_rep;
        }

        // subtract first sample in sliding boxcar avg
        if (tot_samples_rep > MINIMUM_SAMPLES) {
            oldAccMag = newAccMag;
            newAccMag -= accDataArray[cycle_count_rep - avglen_rep];
        }

        // place current sample data in buffer
        accDataArray[cycle_count_rep] = acc_mag;
        //LOGD("accDataArray[cycle_count_rep]:%i : %i", cycle_count_rep, accDataArray[cycle_count_rep]);

        newAccMag += acc_mag; // add new sample to sliding boxcar avg
        if ((abs(newAccMag - oldAccMag)) < 1 ) //avgthresh_rep)
            newAccMag = oldAccMag;

        // Conditions to store Maximum & Minimum AccMag value.
        if (acc_mag > newmax_rep)
            newmax_rep = acc_mag;
        if (acc_mag < newmin_rep)
            newmin_rep = acc_mag;

        // increment count of samples in current step
        tot_samples_rep++;
        cycle_count_rep++;

        if (tot_samples_rep > 6) {
            if (IsStep_rep()){ //newAccMag, oldAccMag)) {
                int diff = abs(newmax_rep - newmin_rep);
                if (diff > MAX_MIN_DIFF) {
                    rep_count_var++; // += 0.5;
                    // mpu_data_r->rep_count_strt = rep_count_var;
                    // _force(power, &gyro_mag, &acc_mag, tot_force, tot_velo);
                    LOGE("DIFF: %i, rep_count_var: %d", diff, rep_count_var);
                }

                step_cal_incre_rep += 2;
                count_idle_rep = 0;

                // need all data used in calculating newavg
                for (int i = 0; i < avglen_rep; i++)
                    accDataArray[i] = accDataArray[cycle_count_rep + i - (avglen_rep)];

                cycle_count_rep = avglen_rep;
                maxavg_rep = -5.0;
                minavg_rep = 5.0;
                newmax_rep = -5.0;
                newmin_rep = 5.0;
            } // if IsStep condition Completed.
        }
        return rep_count_var;
    }

    //**********************************************************************************************
    void _force(float *_power, float *gyro_data_mag_rec, float *accel_data_mag_rec, float *force,
                float *velocity) {
        *velocity = *gyro_data_mag_rec * 0.01745;//Concersion of rad/sec
        *velocity = *velocity * 0.33;//rad/sec x ARM Length in Meter
        *force = *accel_data_mag_rec * 9.81;//Mag x 9.81 x weight Lifted for future referance
        *_power = *force * *velocity;
    }

    //**********************************************************************************************
    char IsStep_rep() { //float newAccMag, float oldAccMag) {
        //LOGD("avg_rep: %i - %i = %i", avg_rep, oldAccMag, (avg_rep-oldAccMag));
        // this function attempts to determine when a step is complete

        LOGD("************************ stepflag: %i, frameCount: %i, cycleCount: %i", stepflag_rep, frameCount++, cycle_count_rep);
        LOGD("newAccMag: %i, oldAccMag: %i", newAccMag, oldAccMag);
        LOGD("maxavg: %i, minavg_rep: %i", maxavg_rep, minavg_rep);

        float step_thresh_rep = 5.0; // used to prevent noise from fooling the algorithm

        if (stepflag_rep == 2) {
            if (newAccMag > (oldAccMag + step_thresh_rep))
                stepflag_rep = 1;
            if (newAccMag < (oldAccMag - step_thresh_rep))
                stepflag_rep = 0;
            return 0;
        } // first time through this function

        if (stepflag_rep == 1) {
            if ((maxavg_rep > minavg_rep) && (newAccMag >
                                              ((maxavg_rep + minavg_rep) / 2)) &&
                (oldAccMag < ((maxavg_rep + minavg_rep / 2))))
                return 1;
            if (newAccMag < (oldAccMag - step_thresh_rep)) {
                stepflag_rep = 0;
                if (oldAccMag > maxavg_rep)
                    maxavg_rep = oldAccMag;
            } // slope has turned down
            return 0;
        } // slope has been up

        if (stepflag_rep == 0) {
            if (newAccMag > (oldAccMag + step_thresh_rep)) {
                stepflag_rep = 1;
                if (oldAccMag < minavg_rep)
                    minavg_rep = oldAccMag;
            } // slope has turned up
            return 0;
        } // slope has been down

        return 0;
    } // IsStep()

    double getRepCount() {
        return rep_count_var;
    }
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
    return env->NewStringUTF(nativeLibObj.helloString().c_str());
}

JNIEXPORT double JNICALL
Java_com_nikhil_phone_callnative_CallNativeFunctions_calculateMag(
        JNIEnv *env,
        jobject,
        double x, double y, double z) {

    return nativeLibObj.calculateMag(x, y, z);
}

JNIEXPORT int JNICALL
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

    return nativeLibObj.call_rep(&mpu21);

}

JNIEXPORT double JNICALL
Java_com_nikhil_phone_callnative_CallNativeFunctions_getRepValue(
        JNIEnv *env,
        jobject) {

    return nativeLibObj.getRepCount();

}

}