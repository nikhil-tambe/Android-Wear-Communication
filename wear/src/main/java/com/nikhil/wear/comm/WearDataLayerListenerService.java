package com.nikhil.wear.comm;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.nikhil.wear.sensors.SensorService;
import com.nikhil.wear.ui.activities.MainActivity;

import java.util.concurrent.TimeUnit;

import static com.nikhil.shared.Constants.ChannelC.PATH_COUNT;
import static com.nikhil.shared.Constants.ChannelC.PATH_DATA_ITEM_RECEIVED;
import static com.nikhil.shared.Constants.ChannelC.PATH_START_APP;
import static com.nikhil.shared.Constants.ChannelC.PATH_START_SENSOR_SERVICE;
import static com.nikhil.shared.Constants.ChannelC.PATH_STOP_SENSOR_SERVICE;

/**
 * Created by Nikhil on 19/7/17.
 */

public class WearDataLayerListenerService extends WearableListenerService {

    public static final String TAG = "nikhil DataListener";
    GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
            ConnectionResult connectionResult = googleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "WearDataLayerListenerService failed to connect to GoogleApiClient, "
                        + "error code: " + connectionResult.getErrorCode());
                return;
            }
        }

        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : dataEventBuffer) {
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();
            Log.d(TAG, "onDataChanged: " + path);
            if (PATH_COUNT.equals(path)) {
                // Get the node id of the node that created the data item from the host portion of
                // the uri.
                String nodeId = uri.getHost();
                // Set the data of the message to be the bytes of the Uri.
                byte[] payload = uri.toString().getBytes();

                // Send the rpc
                Wearable.MessageApi.sendMessage(googleApiClient, nodeId, PATH_DATA_ITEM_RECEIVED,
                        payload);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent.getPath() + ": " + messageEvent.toString());

        switch (messageEvent.getPath()) {

            case PATH_START_APP:
                String message = new String(messageEvent.getData()); //, Charset.forName("UTF-8"));
                Intent startIntent = new Intent(this, MainActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startIntent.putExtra(PATH_START_APP, message);
                startActivity(startIntent);
                break;

            case PATH_START_SENSOR_SERVICE:
                startService(new Intent(this, SensorService.class));
                break;

            case PATH_STOP_SENSOR_SERVICE:
                stopService(new Intent(this, SensorService.class));
                break;

        }

    }

}
