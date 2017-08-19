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
import com.nikhil.phone.callnative.CallNativeFunctions;
import com.nikhil.phone.comm.SendMessageAsyncTask;
import com.nikhil.shared.CheckConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
import static com.nikhil.shared.Constants.StorageC.SAMPLE_INDEX_CSV;
import static com.nikhil.shared.Constants.StorageC.SESSION_LOG_CSV;

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
    @BindView(R.id.repCount_TextView)
    TextView repCount_TextView;
    File sessionCSV, sampleIndexFile;
    FileWriter mFileWriter;
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

        Log.d(TAG, "onCreate: " + CallNativeFunctions.helloWorld());

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSensorButton_Clicked();
    }

    @OnClick(R.id.startApp_Button)
    public void startAppButton_Clicked() {
        new SendMessageAsyncTask(googleApiClient, PATH_START_APP)
                .execute(messageInput_EditText.getText().toString());
    }

    @OnClick(R.id.startSensorOnWear_Button)
    public void startSensorButton_Clicked() {
        Log.d(TAG, "startSensorButtonClicked: ");
        /*Intent sensorService = new Intent(this, SensorService.class);
        startService(sensorService);*/
        createFiles();
        new SendMessageAsyncTask(googleApiClient, PATH_START_SENSOR_SERVICE)
                .execute("message-start-sensor-service");
    }

    @OnClick(R.id.stopSensorOnWear_Button)
    public void stopSensorButton_Clicked() {
        Log.d(TAG, "stopSensorButtonClicked: ");
        /*Intent sensorService = new Intent(this, SensorService.class);
        stopService(sensorService);*/
        new SendMessageAsyncTask(googleApiClient, PATH_STOP_SENSOR_SERVICE)
                .execute("message-stop-sensor-service");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setSensorTextView("", "", "");
            }
        }, 1000);
    }

    @OnClick(R.id.calcReps_Button)
    public void calcRepsButton_Clicked() {
        CallNativeFunctions.helloWorld();
        try {
            sessionCSV = new File(getExternalFilesDir(null), SESSION_LOG_CSV);
            if (sessionCSV.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(sessionCSV));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] raw_values = line.split(",");
                    Float ax = Float.parseFloat(raw_values[0]);
                    Float ay = Float.parseFloat(raw_values[1]);
                    Float az = Float.parseFloat(raw_values[2]);
                    //Float gx = Float.parseFloat(raw_values[3]);
                    //Float gy = Float.parseFloat(raw_values[4]);
                    //Float gz = Float.parseFloat(raw_values[5]);
                    CallNativeFunctions.calcRep(ax, ay, az); //, gx, gy, gz);
                }
            } else {
                Toast.makeText(this, "No Session File", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        repCount_TextView.setText("" + CallNativeFunctions.getRepValue());

        writeSampleIndex(CallNativeFunctions.getRepSampleIndexCSV());

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "API Connected", Toast.LENGTH_SHORT).show();
        resolvingError = false;
        startApp_Button.setEnabled(true);
        addListeners();
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
                removeListeners();
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

                if (path.startsWith(PATH_SENSOR_DATA)) { // + "/")) {
                    unpackSensorData(DataMapItem.fromDataItem(dataItem).getDataMap());
                } else {
                    Log.d(TAG, "onDataChanged: " + path);
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
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BODY_SENSORS,
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

    private void createFiles() {
        Log.d(TAG, "createFiles: ");
        try {
            sessionCSV = new File(getExternalFilesDir(null), SESSION_LOG_CSV);
            sampleIndexFile = new File(getExternalFilesDir(null), SAMPLE_INDEX_CSV);
            if (sessionCSV.exists()) {
                Log.d(TAG, "createFiles: " + sessionCSV.delete() + ", " + sampleIndexFile.delete());
                sessionCSV = new File(getExternalFilesDir(null), SESSION_LOG_CSV);
                sampleIndexFile = new File(getExternalFilesDir(null), SAMPLE_INDEX_CSV);
            }
            mFileWriter = new FileWriter(sessionCSV, true);
        } catch (Exception e) {
            Toast.makeText(this, "writer error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void addListeners() {
        Wearable.DataApi.addListener(googleApiClient, this);
        Wearable.MessageApi.addListener(googleApiClient, this);
        Wearable.CapabilityApi.addListener(
                googleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);
    }

    private void removeListeners() {
        Wearable.DataApi.removeListener(googleApiClient, this);
        Wearable.MessageApi.removeListener(googleApiClient, this);
        Wearable.CapabilityApi.removeListener(googleApiClient, this);
    }

    private void unpackSensorData(DataMap dataMap) {
        int accuracy = dataMap.getInt(ACCURACY);
        long timestamp = dataMap.getLong(TIMESTAMP);
        String acc_values = dataMap.getString(ACC_VALUES);
        String gyro_values = dataMap.getString(GYRO_VALUES);
        String hr_values = dataMap.getString(HR_VALUES);

        setSensorTextView(acc_values, gyro_values, hr_values);

        String[] acc_array = acc_values.split(",");
        Float ax = Float.parseFloat(acc_array[0]);
        Float ay = Float.parseFloat(acc_array[1]);
        Float az = Float.parseFloat(acc_array[2]);

        String[] gyro_array = gyro_values.split(",");
        Float gx = Float.parseFloat(gyro_array[0]);
        Float gy = Float.parseFloat(gyro_array[1]);
        Float gz = Float.parseFloat(gyro_array[2]);

        //CallNativeFunctions.helloWorld();

        repCount_TextView.setText("" + CallNativeFunctions.calcRep(ax, ay, az)); //, gx, gy, gz));

        writeLog(acc_values + "," + CallNativeFunctions.calculateMag(ax, ay, az));//gyro_values + "," + hr_values);
        //CallNativeFunctions.calculateMag(ax, ay, az));
    }

    private void writeLog(String data) {
        try {
            mFileWriter = new FileWriter(sessionCSV, true);
            mFileWriter.append(data);
            mFileWriter.append("\n");
            mFileWriter.close();
            //Log.d(TAG, "writeLog() Sensor Data: " + data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeSampleIndex(String data) {
        try {
            sampleIndexFile = new File(getExternalFilesDir(null), SAMPLE_INDEX_CSV);
            if (sampleIndexFile.exists()){
                sampleIndexFile.delete();
                sampleIndexFile = new File(getExternalFilesDir(null), SAMPLE_INDEX_CSV);
            }
            for (String s : data.split(",")){
                FileWriter fileWriter = new FileWriter(sampleIndexFile, true);
                fileWriter.append(s);
                fileWriter.append("\n");
                fileWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSensorTextView(String acc_values, String gyro_values, String hr_values) {
        sensorAccData_TextView.setText(acc_values);
        sensorGyroData_TextView.setText(gyro_values);
        sensorHRData_TextView.setText(hr_values);
    }

}
