package com.nikhil.shared;

/**
 * Created by Nikhil on 15/7/17.
 */

public interface Constants {

    interface Capabilities {
        String VOICE_TRANSCRIPTION_CAPABILITY_NAME = "voice_transcription";

    }

    interface ChannelC {
        String MESSAGE_CHANNEL = "/mess";
        String START_ACTIVITY_PATH = "/start-activity";
        String COUNT_PATH = "/count";
        String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
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
