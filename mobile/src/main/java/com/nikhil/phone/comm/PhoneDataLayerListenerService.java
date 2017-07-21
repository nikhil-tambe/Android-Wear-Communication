package com.nikhil.phone.comm;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.nikhil.phone.app.ApplicationClass;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.nikhil.shared.Constants.ChannelC.CHANNEL_SESSION;
import static com.nikhil.shared.Constants.ChannelC.CHANNEL_SESSION_DATE;
import static com.nikhil.shared.Constants.ChannelC.PATH_DATA_ITEM_RECEIVED;
import static com.nikhil.shared.Constants.ChannelC.PATH_MESSAGE;
import static com.nikhil.shared.Constants.StorageC.SESSION_DATE_CSV;
import static com.nikhil.shared.Constants.StorageC.SESSION_LOG_CSV;
import static com.nikhil.shared.Constants.StorageC.UNKNOW_FILE_TXT;

/**
 * Created by Nikhil on 19/7/17.
 */

public class PhoneDataLayerListenerService extends WearableListenerService {

    private static final String TAG = "nikhil " + PhoneDataLayerListenerService.class.getSimpleName();
    Context context;
    GoogleApiClient googleApiClient;
    File sessionFile, dateFile, unknownFile;

    public PhoneDataLayerListenerService() {
        context = ApplicationClass.getAppContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        sessionFile = new File(getExternalFilesDir(null), SESSION_LOG_CSV);
        dateFile = new File(getExternalFilesDir(null), SESSION_DATE_CSV);
        unknownFile = new File(getExternalFilesDir(null), UNKNOW_FILE_TXT);
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
                    PATH_DATA_ITEM_RECEIVED, payload);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(PATH_MESSAGE)) {

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
        Log.d(TAG, "onChannelOpened: " + channel.getPath());
        switch (channel.getPath()){

            case CHANNEL_SESSION:
                receiveFile(channel, sessionFile);
                break;

            case CHANNEL_SESSION_DATE:
                receiveFile(channel, dateFile);
                break;

            default:
                receiveFile(channel, unknownFile);
                break;
        }

    }

    private void receiveFile(final Channel channel, final File file) {
        channel.receiveFile(googleApiClient, Uri.fromFile(file), false)
                .setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.d(TAG, "onResult: " + file.getName() + " : " + status);
                Toast.makeText(context,
                        file.getName() + " received. " + status ,
                        Toast.LENGTH_SHORT).show();
                renameFile(channel.getPath(), file);
            }
        });
    }

    private void renameFile(String path, File file) {
        String s = new SimpleDateFormat("yyyyMMddkkmmss", Locale.ENGLISH).format(new Date()) + ".csv";
        switch (path){
            case CHANNEL_SESSION:
                File sessionFile = new File(getExternalFilesDir(null) + "/session_" + s);
                Log.e(TAG, "renamedFile to session_" + s + ": " + file.renameTo(sessionFile));
                break;

            case CHANNEL_SESSION_DATE:
                File sessionDateFile = new File(getExternalFilesDir(null) + "/sessionDate_" + s);
                Log.e(TAG, "renamedFile to sessionDate_" + s + ": " + file.renameTo(sessionDateFile));
                break;

            default:
                File newUnknownFile = new File(getExternalFilesDir(null)+ "/unknown_" + s);
                Log.e(TAG, "renamedFile to unknown_" + s + ": " + file.renameTo(newUnknownFile));
                break;

        }
    }

    @Override
    public void onChannelClosed(Channel channel, int i, int i1) {
        Log.d(TAG, "onChannelClosed: " + channel.getPath());
    }

    @Override
    public void onInputClosed(Channel channel, int i, int i1) {
        Log.d(TAG, "onInputClosed: " + channel.getPath());
    }

    @Override
    public void onOutputClosed(Channel channel, int i, int i1) {
        Log.d(TAG, "onOutputClosed: " + channel.getPath());
    }

}
