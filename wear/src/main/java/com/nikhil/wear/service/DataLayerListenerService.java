package com.nikhil.wear.service;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.nikhil.wear.ui.activities.MainActivity;

import java.util.concurrent.TimeUnit;

import static com.nikhil.shared.Constants.ChannelC.PATH_COUNT;
import static com.nikhil.shared.Constants.ChannelC.PATH_DATA_ITEM_RECEIVED;
import static com.nikhil.shared.Constants.ChannelC.PATH_START_ACTIVITY;

/**
 * Created by Nikhil on 19/7/17.
 */

public class DataLayerListenerService extends WearableListenerService{
        //implements GoogleApiClient.ConnectionCallbacks {

    public static final String TAG = "nikhil DataListener";
    GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                //.addConnectionCallbacks(this)
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
                Log.e(TAG, "DataLayerListenerService failed to connect to GoogleApiClient, "
                        + "error code: " + connectionResult.getErrorCode());
                return;
            }
        }

        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : dataEventBuffer) {
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();
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
        if (messageEvent.getPath().equals(PATH_START_ACTIVITY)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }

    /*@Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "api connected onConnected: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }*/
}
