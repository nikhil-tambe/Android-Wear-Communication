package com.nikhil.wear.ui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nikhil.wear.R;
import com.nikhil.wear.comm.WearChannelAsync;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nikhil.shared.Constants.ChannelC.CHANNEL_SESSION;
import static com.nikhil.shared.Constants.MathC.FRAME_RATE;
import static com.nikhil.shared.Constants.MathC.p;
import static com.nikhil.shared.Constants.MathC.pd;
import static com.nikhil.shared.Constants.StorageC.SESSION_LOG_CSV;

/**
 * Created by Nikhil on 20/7/17.
 */

public class SensorActivity extends WearableActivity implements SensorEventListener {

    public static final String TAG = "nikhil " + SensorActivity.class.getSimpleName();

    @BindView(R.id.startSensor_Button)
    Button startSensor_Button;
    @BindView(R.id.stopSensor_Button)
    Button stopSensor_Button;
    @BindView(R.id.sendFile_Button)
    Button sendFile_Button;
    @BindView(R.id.accData_TextView)
    TextView accData_TextView;
    @BindView(R.id.gyroData_TextView)
    TextView gyroData_TextView;
    @BindView(R.id.hrData_TextView)
    TextView hrData_TextView;

    Context context;
    SensorManager sensorManager;
    Sensor accSensor, gyroSensor, hrSensor;
    String acc_data = "0,0,0";
    String gyro_data = "0,0,0";
    String hr_data = "72.0";
    int teCount = 0;
    File sessionCSV;
    SimpleDateFormat sdf;
    boolean sensorStatus;
    FileWriter mFileWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        context = this;
        ButterKnife.bind(this);
        setAmbientEnabled();
    }

    @Override
    protected void onStart() {
        super.onStart();
        stopSensor_Button.setVisibility(View.GONE);
        sendFile_Button.setVisibility(View.GONE);
        sdf = new SimpleDateFormat("HHmmss", Locale.getDefault());
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        createFiles();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterSensor();
            mFileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return; // If sensor is unreliable, then just return
        }
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acc_data = togglePrecision(event.values[0]) + ","
                    + togglePrecision(event.values[1]) + ","
                    + togglePrecision(event.values[2]);
            accData_TextView.setText(acc_data);
        }
        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyro_data = togglePrecision(event.values[0]) + ","
                    + togglePrecision(event.values[1]) + ","
                    + togglePrecision(event.values[2]);
            gyroData_TextView.setText(gyro_data);
        }
        if (sensor.getType() == Sensor.TYPE_HEART_RATE) {
            hr_data = Float.toString(event.values[0]);
            hrData_TextView.setText(hr_data);
        }
        String data = acc_data + "," + gyro_data + "," + hr_data + "," + 0
                + "," + sdf.format(new Date());

        writeLog(data);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
    }

    @OnClick(R.id.startSensor_Button)
    public void startSensor_ButtonClicked() {
        startSensor_Button.setVisibility(View.GONE);
        stopSensor_Button.setVisibility(View.VISIBLE);
        sendFile_Button.setVisibility(View.GONE);
        registerSensor();
    }

    @OnClick(R.id.stopSensor_Button)
    public void stopSensor_ButtonClicked() {
        startSensor_Button.setVisibility(View.GONE);
        stopSensor_Button.setVisibility(View.GONE);
        sendFile_Button.setVisibility(View.VISIBLE);
        unregisterSensor();
    }

    @OnClick(R.id.sendFile_Button)
    public void sendFile_ButtonClicked() {
        startSensor_Button.setVisibility(View.VISIBLE);
        stopSensor_Button.setVisibility(View.GONE);
        sendFile_Button.setVisibility(View.GONE);
        sendFile(sessionCSV, CHANNEL_SESSION);
    }

    private void sendFile(File file, String channelName) {
        new WearChannelAsync(context, Uri.fromFile(file), channelName).execute();
    }

    private void registerSensor() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
            sensorManager.registerListener(this, hrSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void unregisterSensor() {
        sensorManager.unregisterListener(this, accSensor);
        sensorManager.unregisterListener(this, gyroSensor);
        sensorManager.unregisterListener(this, hrSensor);

    }

    private double togglePrecision(float d) {
        return Math.round(d * p) / pd;
    }

    private void createFiles() {
        Log.d(TAG, "createFiles: ");
        try {
            sessionCSV = new File(getExternalFilesDir(null), SESSION_LOG_CSV);
            if (sessionCSV.exists()) {
                sessionCSV.delete();
                sessionCSV = new File(getExternalFilesDir(null), SESSION_LOG_CSV);
            }
            mFileWriter = new FileWriter(sessionCSV, true);
        } catch (Exception e) {
            Toast.makeText(context, "writer error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    /**
     * Control frame rate by:
     * %5 = 20fps.
     * %4 = 25fps.
     * %2 = 50fps.
     */
    private void writeLog(String data) {
        try {
            teCount++;
            if (teCount % FRAME_RATE == 0) {
                mFileWriter.append(data);
                mFileWriter.append("\n");
                Log.d(TAG, "writeLog() Sensor Data: " + data);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IO Exception NN: " + e.toString());
        }
    }

}
