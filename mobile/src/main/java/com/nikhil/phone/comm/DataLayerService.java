package com.nikhil.phone.comm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.android.gms.wearable.zzd;
import com.nikhil.phone.app.ApplicationClass;
import com.nikhil.phone.ui.activities.HomeActivity;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.nikhil.shared.Constants.Capabilities.VOICE_TRANSCRIPTION_CAPABILITY_NAME;
import static com.nikhil.shared.Constants.ChannelC.MESSAGE_CHANNEL;

/**
 * Created by Nikhil on 19/7/17.
 */

public class DataLayerService extends WearableListenerService {

    private static final String TAG = "nikhil " + DataLayerService.class.getSimpleName();
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    Context context;
    GoogleApiClient googleApiClient;

    public DataLayerService() {
        context = ApplicationClass.getAppContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onDataChanged: " + dataEvents);
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        // Loop through the events and send a message
        // to the node that created the data item.
        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();

            // Get the node id from the host value of the URI
            String nodeId = uri.getHost();
            // Set the data of the message to be the bytes of the URI
            byte[] payload = uri.toString().getBytes();

            // Send the RPC
            Wearable.MessageApi.sendMessage(googleApiClient, nodeId,
                    DATA_ITEM_RECEIVED_PATH, payload);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(MESSAGE_CHANNEL)) {

            String s = new String(messageEvent.getData());

            Log.d(TAG, "onMessageReceived: " + s);

            Toast.makeText(context, s, Toast.LENGTH_LONG).show();

            /*Intent startIntent = new Intent(this, HomeActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startIntent.putExtra("VOICE_DATA", messageEvent.getData());
            startActivity(startIntent);*/
        }
    }

    @Override
    public void onPeerConnected(Node node) {
        super.onPeerConnected(node);
    }

    @Override
    public void onPeerDisconnected(Node node) {
        super.onPeerDisconnected(node);
    }

    @Override
    public void onConnectedNodes(List<Node> list) {
        super.onConnectedNodes(list);
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
