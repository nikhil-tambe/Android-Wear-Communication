package com.nikhil.wear.service;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.android.gms.wearable.zzd;
import com.nikhil.wear.ui.activities.HomeActivity;

import java.util.concurrent.TimeUnit;

import static com.nikhil.shared.Constants.ChannelC.COUNT_PATH;
import static com.nikhil.shared.Constants.ChannelC.DATA_ITEM_RECEIVED_PATH;
import static com.nikhil.shared.Constants.ChannelC.START_ACTIVITY_PATH;

/**
 * Created by Nikhil on 19/7/17.
 */

public class DataLayerListenerService extends WearableListenerService {

    public static final String TAG = "nikhil DataListener";
    GoogleApiClient googleApiClient;
    
    public DataLayerListenerService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
            if (COUNT_PATH.equals(path)) {
                // Get the node id of the node that created the data item from the host portion of
                // the uri.
                String nodeId = uri.getHost();
                // Set the data of the message to be the bytes of the Uri.
                byte[] payload = uri.toString().getBytes();

                // Send the rpc
                Wearable.MessageApi.sendMessage(googleApiClient, nodeId, DATA_ITEM_RECEIVED_PATH,
                        payload);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(START_ACTIVITY_PATH)) {
            Intent startIntent = new Intent(this, HomeActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        super.onCapabilityChanged(capabilityInfo);
    }

    @Override
    public void onChannelOpened(Channel channel) {
        super.onChannelOpened(channel);
    }

    @Override
    public void onChannelClosed(Channel channel, int i, int i1) {
        super.onChannelClosed(channel, i, i1);
    }

    @Override
    public void onInputClosed(Channel channel, int i, int i1) {
        super.onInputClosed(channel, i, i1);
    }

    @Override
    public void onOutputClosed(Channel channel, int i, int i1) {
        super.onOutputClosed(channel, i, i1);
    }

    @Override
    public void onNotificationReceived(zzd zzd) {
        super.onNotificationReceived(zzd);
    }
}
