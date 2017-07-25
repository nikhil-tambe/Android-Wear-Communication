package com.nikhil.shared;

/**
 * Created by Nikhil on 15/7/17.
 */

public interface Constants {

    interface ChannelC {
        String PATH_MESSAGE = "/mess";
        String PATH_START_APP = "/start-app";
        String PATH_COUNT = "/count";
        String PATH_DATA_ITEM_RECEIVED = "/data-item-received";
        String PATH_IMAGE = "/image";
        String PATH_START_SENSOR_SERVICE = "/start-sensor-service";
        String PATH_STOP_SENSOR_SERVICE = "/stop-sensor-service";
        String PATH_SENSOR_DATA = "/sensor-data";

        String KEY_IMAGE = "photo";

        String CHANNEL_SESSION = "channelSession";
        String CHANNEL_SESSION_DATE = "channelDate";

    }

    interface SPF {
        String SPF_APP = "spf_app";
        String KEY_DEVICE_NAME = "device_name";
        String KEY_DEVICE_MODEL = "device_model";
        String KEY_DEVICE_VERSION = "device_version";
        String KEY_DEVICE_MANUFACTURER = "device_manufacturer";
    }

    interface StorageC {
        String SESSION_LOG_CSV = "/session_log.csv";
        String SESSION_DATE_CSV = "/session_date.csv";
        String UNKNOW_FILE_TXT = "/unknown.txt";

        String SESSION_LOG_PREFIX = "/session_";
        String SESSION_DATE_PREFIX = "/sessionDate_";
        String UNKNOWN_PREFIX = "/unknown_";
    }

    interface IntentC {
        int REQUEST_RESOLVE_ERROR = 1000;
        int REQUEST_CODE_GROUP_PERMISSIONS = 123;
    }

    interface MathC {
        int FRAME_RATE = 2;
        // for 6 digit precision
        int p = 1000000;
        double pd = 1000000.0d;
    }

    interface DataMapKeys {
        String ACCURACY = "accuracy";
        String TIMESTAMP = "timestamp";
        String ACC_VALUES = "acc_values";
        String GYRO_VALUES = "gyro_values";
        String HR_VALUES = "hr_values";
        String FILTER = "filter";
    }

    interface GENERAL {
        int CLIENT_CONNECTION_TIMEOUT = 15000;
        String REVERSE_DATE_FORMAT = "yyyyMMddkkmmss";
    }

}
