package com.nikhil.phone.ui.activities;

import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.nikhil.phone.R;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nikhil.shared.Constants.ChannelC.PATH_SENSOR_DATA;
import static com.nikhil.shared.Constants.ChannelC.PATH_START_APP;
import static com.nikhil.shared.Constants.ChannelC.PATH_START_SENSOR_SERVICE;
import static com.nikhil.shared.Constants.ChannelC.PATH_STOP_SENSOR_SERVICE;
import static com.nikhil.shared.Constants.DataMapKeys.ACCURACY;
import static com.nikhil.shared.Constants.DataMapKeys.TIMESTAMP;
import static com.nikhil.shared.Constants.DataMapKeys.VALUES;
import static com.nikhil.shared.Constants.IntentC.REQUEST_RESOLVE_ERROR;

/**
 * Created by Nikhil on 19/7/17.
 */

public class CommActivity extends AppCompatActivity
        implements CapabilityApi.CapabilityListener,
        MessageApi.MessageListener,
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "nikhil " + CommActivity.class.getSimpleName();
    GoogleApiClient googleApiClient;
    @BindView(R.id.messageInput_EditText)
    EditText messageInput_EditText;
    @BindView(R.id.startApp_Button)
    Button startApp_Button;
    String path;
    private boolean resolvingError = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_message);

        ButterKnife.bind(this);

        //startService(new Intent(this, PhoneDataLayerListenerService.class));

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!resolvingError) {
            googleApiClient.connect();
        } else {
            Log.d(TAG, "onStart: Resolving Error " + resolvingError);
        }
    }

    @OnClick(R.id.startApp_Button)
    public void startAppButton_Clicked() {
        path = PATH_START_APP;
        new StartWearableActivityTask().execute(messageInput_EditText.getText().toString());
    }

    @OnClick(R.id.startSensorOnWear_Button)
    public void startSensorButtonClicked() {
        path = PATH_START_SENSOR_SERVICE;
        new StartWearableActivityTask().execute("start-sensor-service");
    }

    @OnClick(R.id.stopSensorOnWear_Button)
    public void stopSensorButtonClicked() {
        path = PATH_STOP_SENSOR_SERVICE;
        new StartWearableActivityTask().execute("stop-sensor-service");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "API Connected", Toast.LENGTH_SHORT).show();
        resolvingError = false;
        startApp_Button.setEnabled(true);
        Wearable.DataApi.addListener(googleApiClient, this);
        Wearable.MessageApi.addListener(googleApiClient, this);
        Wearable.CapabilityApi.addListener(
                googleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);
    }

    @Override
    public void onConnectionSuspended(int i) {
        startApp_Button.setEnabled(false);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!resolvingError) {

            if (connectionResult.hasResolution()) {
                try {
                    resolvingError = true;
                    connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    // There was an error with the resolution intent. Try again.
                    googleApiClient.connect();
                }
            } else {
                Log.e(TAG, "Connection to Google API client has failed");
                resolvingError = false;
                startApp_Button.setEnabled(false);
                Wearable.DataApi.removeListener(googleApiClient, this);
                Wearable.MessageApi.removeListener(googleApiClient, this);
                Wearable.CapabilityApi.removeListener(googleApiClient, this);
            }
        }
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged: onCapabilityChanged: " + capabilityInfo.toString());
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent dataEvent : dataEventBuffer) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                if (path.startsWith(PATH_SENSOR_DATA + "/")) {
                    unpackSensorData(
                            Integer.parseInt(uri.getLastPathSegment()),
                            DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: Message from watch: " + messageEvent.toString());
    }

    private void unpackSensorData(int sensorType, DataMap dataMap) {
        int accuracy = dataMap.getInt(ACCURACY);
        long timestamp = dataMap.getLong(TIMESTAMP);
        float[] values = dataMap.getFloatArray(VALUES);

        Log.d(TAG, "Received sensor data " + sensorType + " = " + Arrays.toString(values));

        //sensorManager.addSensorData(sensorType, accuracy, timestamp, values);
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

        for (Node node : nodes.getNodes()) {
            Log.d(TAG, "getNodes: " + node.getId()
                    + ": " + node.getDisplayName() + ": " + node.isNearby());
            results.add(node.getId());
        }

        return results;
    }

    private void sendStartActivityMessage(final String node, String s) {

        byte[] message = s.getBytes(); //Charset.forName("UTF-8"));

        Wearable.MessageApi
                .sendMessage(googleApiClient, node, path, message)
                .setResultCallback(
                        new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "Failed to send message with status code: "
                                            + sendMessageResult.getStatus().getStatusCode());
                                } else {
                                    Log.d(TAG, "onResult: message sent to " + node);
                                }
                            }
                        }
                );
    }

    private class StartWearableActivityTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... args) {
            Collection<String> nodes = getNodes();
            String s = args[0]; //"asd";
            for (String node : nodes) {
                sendStartActivityMessage(node, s);
            }
            return null;
        }
    }
}
