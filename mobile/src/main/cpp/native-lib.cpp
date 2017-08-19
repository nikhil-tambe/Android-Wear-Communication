#include <jni.h>
#include <math.h>
#include <string>
#include <vector>
#include <sstream>
#include <iostream>
#include "stdlib.h"

#include "native-lib.h"

class native_lib {

    int repCount, repFlag, frameCount, cycle_count_rep, avglen_rep;
    float tot_samples_rep;
    double newAccMag, oldAccMag, newmax_rep, newmin_rep, maxavg_rep, minavg_rep;
    double accDataArray[WINDOW_SIZE];
    int sampleIndexArray[1000];
    float tot_force, tot_velo, power;
    std::string s = "hello NDK", sampleIndexCSV;
    char arrr[1000];

public:

    //**********************************************************************************************
    std::string helloString() {
        //LOGD("repCount %f", repCount);
        //LOGD("frameCount %i", frameCount++);
        frameCount = 0;
        repFlag = 2;
        avglen_rep = 5;
        maxavg_rep = -10000.0;
        minavg_rep = 10000.0;
        newmax_rep = -10000.0;
        newmin_rep = 10000.0;
        return s;
    }

    //**********************************************************************************************
    double calculateMag(double x, double y, double z) {

        double sum = pow(x, 2) + pow(y, 2) + pow(z, 2);

        return sqrt(sum);
    }

    //**********************************************************************************************
    int call_rep(mpu2 *mpu_data_r) {

        double acc_mag, gyro_mag, Ax, Ay, Az, Gx, Gy, Gz;
        //, Ay_rep, Ax_rep, Az_rep, Gx_rep, Gy_rep, Gz_rep;
        frameCount++;

        Ax = (mpu_data_r->x_accel);
        Ay = (mpu_data_r->y_accel);
        Az = (mpu_data_r->z_accel);

        acc_mag = sqrt((Ax * Ax) + (Ay * Ay) + (Az * Az));
        LOGD("****************************** acc_mag = %f", acc_mag);

        if (cycle_count_rep >= WINDOW_SIZE) {
            /*for (int i = 0; i < avglen_rep; i++) {
                accDataArray[i] = accDataArray[cycle_count_rep + i - (avglen_rep)];
            }
            cycle_count_rep = avglen_rep;*/
            cycle_count_rep = 7;
            tot_samples_rep = 7;
        }

        /*if (tot_samples_rep >= WINDOW_SIZE) {
            tot_samples_rep = avglen_rep;
        }*/

        // subtract first sample in sliding boxcar avg
        if (tot_samples_rep > MINIMUM_SAMPLES) {
            oldAccMag = newAccMag;
            newAccMag -= accDataArray[cycle_count_rep - avglen_rep];
        }

        // place current sample data in buffer
        accDataArray[cycle_count_rep] = acc_mag;

        // add new sample to sliding boxcar avg
        newAccMag += acc_mag;

        if ((abs(newAccMag - oldAccMag)) < AVG_THRESHOLD)
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
            if (isRep(newAccMag, oldAccMag)) {
                double diff = abs(newmax_rep - newmin_rep);
                if (diff > MAX_MIN_DIFF) {
                    repCount++; // += 0.5;
                    // mpu_data_r->rep_count_strt = repCount;
                    // _force(power, &gyro_mag, &acc_mag, tot_force, tot_velo);
                    LOGE("DIFF: %f, repCount: %d", diff, repCount);
                    sampleIndexArray[repCount] = frameCount;
                }

                // need all data used in calculating newavg
                for (int i = 0; i < avglen_rep; i++)
                    accDataArray[i] = accDataArray[cycle_count_rep + i - (avglen_rep)];

                cycle_count_rep = avglen_rep;
                maxavg_rep = -10000.0;
                minavg_rep = 10000.0;
                newmax_rep = -10000.0;
                newmin_rep = 10000.0;
            } // if IsStep condition Completed.
        }
        return repCount;
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
    char isRep(double newAccMag, double oldAccMag) {
        // this function attempts to determine when a rep is complete

        LOGD("stepflag: %i, frameCount: %i, cycleCount: %i",
             repFlag, frameCount, cycle_count_rep);
        LOGD("newAccMag: %f, oldAccMag: %f", newAccMag, oldAccMag);
        LOGD("maxavg: %f, minavg_rep: %f", maxavg_rep, minavg_rep);

        if (repFlag == 2) {
            if (newAccMag > (oldAccMag + STEP_THRESHOLD))
                repFlag = 1;
            if (newAccMag < (oldAccMag - STEP_THRESHOLD))
                repFlag = 0;
            return 0;
        } // first time through this function

        if (repFlag == 1) {
            if ((maxavg_rep > minavg_rep)
                && (newAccMag > ((maxavg_rep + minavg_rep) / 2))
                && (oldAccMag < ((maxavg_rep + minavg_rep / 2))))
                return 1;
            if (newAccMag < (oldAccMag - STEP_THRESHOLD)) {
                repFlag = 0;
                if (oldAccMag > maxavg_rep)
                    maxavg_rep = oldAccMag;
            } // slope has turned down
            return 0;
        } // slope has been up

        if (repFlag == 0) {
            if (newAccMag > (oldAccMag + STEP_THRESHOLD)) {
                repFlag = 1;
                if (oldAccMag < minavg_rep)
                    minavg_rep = oldAccMag;
            } // slope has turned up
            return 0;
        } // slope has been down

        return 0;
    } // isRep()

    //*********************************************************************************************
    double getRepCount() {
        //getSampleIndex();
        return repCount;
    }

    //*********************************************************************************************
    std::string getSampleIndex() {
        for (int i = 0; i <= repCount; ++i) {
            int sampleNumber = sampleIndexArray[i];
            if (sampleNumber > 0) {
                //sampleIndexCSV += sampleNumber + ",";
                sprintf(arrr, "%i", sampleNumber);
                sampleIndexCSV.append(arrr);
                sampleIndexCSV.append(",");
                LOGE("Sample Array: %s", sampleIndexCSV.c_str());
            }
        }
        return sampleIndexCSV;
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

JNIEXPORT int JNICALL
Java_com_nikhil_phone_callnative_CallNativeFunctions_calcRep(
        JNIEnv *env,
        jobject,
        double ax, double ay, double az) {
    //,double gx, double gy, double gz) {

    mpu2 mpu21;

    mpu21.x_accel = ax;
    mpu21.y_accel = ay;
    mpu21.z_accel = az;

    /*mpu21.x_gyro = gx;
    mpu21.y_gyro = gy;
    mpu21.z_gyro = gz;*/

    return nativeLibObj.call_rep(&mpu21);

}

JNIEXPORT double JNICALL
Java_com_nikhil_phone_callnative_CallNativeFunctions_getRepValue(
        JNIEnv *env,
        jobject) {

    return nativeLibObj.getRepCount();

}

JNIEXPORT jstring JNICALL
Java_com_nikhil_phone_callnative_CallNativeFunctions_getRepSampleIndexCSV(
        JNIEnv *env,
        jobject) {

    return env->NewStringUTF(nativeLibObj.getSampleIndex().c_str());
}

}