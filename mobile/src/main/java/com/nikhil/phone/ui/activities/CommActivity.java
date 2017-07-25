package com.nikhil.phone.ui.activities;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.wearable.Wearable;
import com.nikhil.phone.R;
import com.nikhil.phone.comm.SendMessageAsyncTask;
import com.nikhil.shared.CheckConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nikhil.shared.Constants.ChannelC.PATH_SENSOR_DATA;
import static com.nikhil.shared.Constants.ChannelC.PATH_START_APP;
import static com.nikhil.shared.Constants.ChannelC.PATH_START_SENSOR_SERVICE;
import static com.nikhil.shared.Constants.ChannelC.PATH_STOP_SENSOR_SERVICE;
import static com.nikhil.shared.Constants.DataMapKeys.ACCURACY;
import static com.nikhil.shared.Constants.DataMapKeys.ACC_VALUES;
import static com.nikhil.shared.Constants.DataMapKeys.GYRO_VALUES;
import static com.nikhil.shared.Constants.DataMapKeys.HR_VALUES;
import static com.nikhil.shared.Constants.DataMapKeys.TIMESTAMP;
import static com.nikhil.shared.Constants.IntentC.REQUEST_CODE_GROUP_PERMISSIONS;
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
    @BindView(R.id.sensorAccData_TextView)
    TextView sensorAccData_TextView;
    @BindView(R.id.sensorGyroData_TextView)
    TextView sensorGyroData_TextView;
    @BindView(R.id.sensorHRData_TextView)
    TextView sensorHRData_TextView;
    private boolean resolvingError = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_message);

        ButterKnife.bind(this);

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
        checkRequiredPermissions();
        CheckConnection checkConnection = new CheckConnection(this);
        if (!checkConnection.checkBTConnection()) {
            checkConnection.enableBluetooth();
        }
    }

    @OnClick(R.id.startApp_Button)
    public void startAppButton_Clicked() {
        new SendMessageAsyncTask(googleApiClient, PATH_START_APP)
                .execute(messageInput_EditText.getText().toString());
    }

    @OnClick(R.id.startSensorOnWear_Button)
    public void startSensorButtonClicked() {
        new SendMessageAsyncTask(googleApiClient, PATH_START_SENSOR_SERVICE)
                .execute("message-start-sensor-service");
    }

    @OnClick(R.id.stopSensorOnWear_Button)
    public void stopSensorButtonClicked() {
        new SendMessageAsyncTask(googleApiClient, PATH_STOP_SENSOR_SERVICE)
                .execute("message-stop-sensor-service");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setSensorTextView("", "", "");
            }
        }, 1000);
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

    private void checkRequiredPermissions() {
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.BODY_SENSORS,
                Manifest.permission.READ_PHONE_STATE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, REQUEST_CODE_GROUP_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_GROUP_PERMISSIONS) {
            for (int resultStatus : grantResults) {
                if (resultStatus == PackageManager.PERMISSION_DENIED) {
                    finish();
                }
            }
        }
    }

    private void unpackSensorData(int sensorType, DataMap dataMap) {
        int accuracy = dataMap.getInt(ACCURACY);
        long timestamp = dataMap.getLong(TIMESTAMP);
        String acc_values = dataMap.getString(ACC_VALUES);
        String gyro_values = dataMap.getString(GYRO_VALUES);
        String hr_values = dataMap.getString(HR_VALUES);

        setSensorTextView(acc_values, gyro_values, hr_values);
    }

    public void setSensorTextView(String acc_values, String gyro_values, String hr_values) {
        sensorAccData_TextView.setText(acc_values);
        sensorGyroData_TextView.setText(gyro_values);
        sensorHRData_TextView.setText(hr_values);
    }

}
