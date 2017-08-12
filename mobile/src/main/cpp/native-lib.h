//
// Created by actofit-android on 28/7/17.
//

#ifndef WEARCOMMUNICATION_NATIVE_LIB_H
#define WEARCOMMUNICATION_NATIVE_LIB_H

#endif //WEARCOMMUNICATION_NATIVE_LIB_H

#include <android/log.h>

#define  LOG_TAG    "nikhil_ndk"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define WINDOW_SIZE 75 //50
#define MAX_MIN_DIFF 5 //850
#define MINIMUM_SAMPLES 5

struct mpu2 {
    uint16_t x_accel;
    uint16_t y_accel;
    uint16_t z_accel;
    uint16_t x_gyro;
    uint16_t y_gyro;
    uint16_t z_gyro;
    uint16_t rep_count_strt;
};
