package com.nikhil.shared;

/**
 * Created by Nikhil on 15/7/17.
 */

public interface Constants {

    interface Capabilities {
        String VOICE_TRANSCRIPTION_CAPABILITY_NAME = "voice_transcription";

    }

    interface ChannelC {
        String PATH_MESSAGE = "/actofit-mess";
        String PATH_START_ACTIVITY = "/actofit-start-activity";
        String PATH_COUNT = "/actofit-count";
        String PATH_DATA_ITEM_RECEIVED = "/actofit-data-item-received";
        String PATH_IMAGE = "/actofit-image";

        String KEY_IMAGE = "actofit-photo";

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
    }

    interface IntentC {
        int REQUEST_RESOLVE_ERROR = 1000;
        int REQUEST_CODE_GROUP_PERMISSIONS = 123;
    }

    interface MathC {
        int FRAME_RATE = 2;
        int p = 1000000;
        double pd = 1000000.0d;
    }

}
