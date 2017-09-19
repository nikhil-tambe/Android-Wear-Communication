package com.nikhil.phone.comm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.nikhil.phone.app.ApplicationClass;
import com.nikhil.phone.ui.activities.CommActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.nikhil.shared.Constants.ChannelC.CHANNEL_SESSION;
import static com.nikhil.shared.Constants.ChannelC.CHANNEL_SESSION_DATE;
import static com.nikhil.shared.Constants.ChannelC.PATH_START_APP;
import static com.nikhil.shared.Constants.GENERAL.REVERSE_DATE_FORMAT;
import static com.nikhil.shared.Constants.StorageC.SESSION_DATE_CSV;
import static com.nikhil.shared.Constants.StorageC.SESSION_DATE_PREFIX;
import static com.nikhil.shared.Constants.StorageC.SESSION_LOG_CSV;
import static com.nikhil.shared.Constants.StorageC.SESSION_LOG_PREFIX;
import static com.nikhil.shared.Constants.StorageC.UNKNOWN_PREFIX;
import static com.nikhil.shared.Constants.StorageC.UNKNOW_FILE_TXT;

/**
 * Created by Nikhil on 19/7/17.
 */

public class PhoneDataLayerListenerService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "nikhil DataLayer";
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

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        switch (messageEvent.getPath()) {

            case PATH_START_APP:
                Intent startIntent = new Intent(this, CommActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(startIntent);
                break;

            default:
                Log.d(TAG, "onMessageReceived: " + messageEvent.getPath()
                        + ": " + new String(messageEvent.getData()));

        }

    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: " + capabilityInfo.toString());
    }

    @Override
    public void onChannelOpened(Channel channel) {
        Log.d(TAG, "onChannelOpened: " + channel.getPath());
        switch (channel.getPath()) {

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

    @Override
    public void onChannelClosed(Channel channel, int i, int i1) {
        Log.d(TAG, "onChannelClosed: " + channel.getPath());
    }

    @Override
    public void onInputClosed(Channel channel, int i, int i1) {
        String path = channel.getPath();
        Log.d(TAG, "onInputClosed: " + path);
        switch (path) {

            case CHANNEL_SESSION:
                renameFile(path, sessionFile);
                break;

            case CHANNEL_SESSION_DATE:
                renameFile(path, dateFile);
                break;

            default:
                renameFile(path, unknownFile);

        }
    }

    @Override
    public void onOutputClosed(Channel channel, int i, int i1) {
        Log.d(TAG, "onOutputClosed: " + channel.getPath());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        Wearable.ChannelApi.addListener(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void receiveFile(final Channel channel, final File file) {
        channel.receiveFile(googleApiClient, Uri.fromFile(file), false)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.d(TAG, "onResult: " + file.getName() + " : " + status);
                    }
                });
    }

    private void renameFile(String path, File file) {
        String s = new SimpleDateFormat(REVERSE_DATE_FORMAT, Locale.ENGLISH).format(new Date()) + ".csv";
        switch (path) {
            case CHANNEL_SESSION:
                File sessionFile = new File(getExternalFilesDir(null) + SESSION_LOG_PREFIX + s);
                Log.e(TAG, "renamedFile to session_" + s + ": " + file.renameTo(sessionFile));
                break;

            case CHANNEL_SESSION_DATE:
                File sessionDateFile = new File(getExternalFilesDir(null) + SESSION_DATE_PREFIX + s);
                Log.e(TAG, "renamedFile to sessionDate_" + s + ": " + file.renameTo(sessionDateFile));
                break;

            default:
                File newUnknownFile = new File(getExternalFilesDir(null) + UNKNOWN_PREFIX + s);
                Log.e(TAG, "renamedFile to unknown_" + s + ": " + file.renameTo(newUnknownFile));
                break;

        }
    }

}
