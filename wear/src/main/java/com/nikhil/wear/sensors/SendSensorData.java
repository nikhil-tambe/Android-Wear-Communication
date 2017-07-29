package com.nikhil.wear.sensors;

import android.content.Context;
import android.util.Log;
import android.util.SparseLongArray;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.nikhil.shared.Constants.ChannelC.PATH_SENSOR_DATA;
import static com.nikhil.shared.Constants.DataMapKeys.ACCURACY;
import static com.nikhil.shared.Constants.DataMapKeys.ACC_VALUES;
import static com.nikhil.shared.Constants.DataMapKeys.GYRO_VALUES;
import static com.nikhil.shared.Constants.DataMapKeys.HR_VALUES;
import static com.nikhil.shared.Constants.DataMapKeys.TIMESTAMP;
import static com.nikhil.shared.Constants.GENERAL.CLIENT_CONNECTION_TIMEOUT;

/**
 * Created by Nikhil on 24/7/17.
 */

public class SendSensorData {

    public static final String TAG = "nikhil " + SendSensorData.class.getSimpleName();
    public static SendSensorData sendSensorDataInstance;
    private GoogleApiClient googleApiClient;
    private ExecutorService executorService;
    private SparseLongArray lastSensorData;

    public SendSensorData(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
        executorService = Executors.newCachedThreadPool();
        lastSensorData = new SparseLongArray();
    }

    public static SendSensorData getInstance(Context context) {
        if (sendSensorDataInstance == null) {
            sendSensorDataInstance = new SendSensorData(context.getApplicationContext());
        }
        return sendSensorDataInstance;
    }

    public void sendData(final int sensorType,
                         final int accuracy,
                         final long timestamp,
                         final String[] values) {

        long t = System.currentTimeMillis();

        /*long lastTimestamp = lastSensorData.get(sensorType);
        long timeAgo = t - lastTimestamp;

        if (lastTimestamp != 0) {
            if (filterId == sensorType && timeAgo < 100) {
                return;
            }

            if (filterId != sensorType && timeAgo < 3000) {
                return;
            }
        }*/

        lastSensorData.put(sensorType, t);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendSensorDataInBackground(sensorType, accuracy, timestamp, values);
            }
        });
    }

    private void sendSensorDataInBackground(int sensorType, int accuracy, long timestamp, String[] values) {

        Log.d(TAG, "Sensor " + sensorType + " = " + values[0] + values[1] + values[2]);

        PutDataMapRequest dataMap = PutDataMapRequest.create(PATH_SENSOR_DATA); // + "/" + sensorType);

        dataMap.getDataMap().putInt(ACCURACY, accuracy);
        dataMap.getDataMap().putLong(TIMESTAMP, timestamp);
        dataMap.getDataMap().putString(ACC_VALUES, values[0]);
        dataMap.getDataMap().putString(GYRO_VALUES, values[1]);
        dataMap.getDataMap().putString(HR_VALUES, values[2]);

        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        send(putDataRequest);
    }

    private void send(PutDataRequest putDataRequest) {
        if (validateConnection()) {
            Wearable.DataApi
                    .putDataItem(googleApiClient, putDataRequest)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            //Log.v(TAG, "Sending sensor data: " + dataItemResult.getStatus().isSuccess());
                        }
                    });
        } else {
            Log.e(TAG, "send: invalid connection");
        }
    }

    private boolean validateConnection() {
        if (googleApiClient.isConnected()) {
            return true;
        }
        ConnectionResult result = googleApiClient.blockingConnect(
                CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        return result.isSuccess();
    }
}
