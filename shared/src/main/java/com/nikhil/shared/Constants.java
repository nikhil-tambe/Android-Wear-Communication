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

    }

    interface SPF {
        String SPF_APP = "spf_app";
        String KEY_DEVICE_NAME = "device_";
        String KEY_DEVICE_MODEL = "device_";
        String KEY_DEVICE_VERSION = "device_";
        String KEY_DEVICE_MANUFACTURER = "device_";
    }

    interface StorageC {

    }

    interface IntentC {
        int REQUEST_RESOLVE_ERROR = 1000;

    }

}
