package com.nikhil.phone.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.nikhil.phone.callnative.CallNativeFunctions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.nikhil.shared.Constants.MathC.p;
import static com.nikhil.shared.Constants.MathC.pd;
import static com.nikhil.shared.Constants.StorageC.SESSION_LOG_CSV;

/**
 * Created by Nikhil on 28/7/17.
 */

public class SensorService extends Service implements SensorEventListener {

    public static final String TAG = "nikhil " + SensorService.class.getSimpleName();
    SensorManager sensorManager;
    Sensor accelerometerSensor;
    String acc_data = "0,0,0";
    String gyro_data = "0,0,0";
    String hr_data = "72.0";
    File sessionCSV;
    FileWriter mFileWriter;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        createFiles();
        registerAvailableSensors();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        unregisterAvailableSensors();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            double x = togglePrecision(event.values[0]);
            double y = togglePrecision(event.values[1]);
            double z = togglePrecision(event.values[2]);

            acc_data = x + "," + y + "," + z;

            double magValue = CallNativeFunctions.calculateMag(x, y, z);

            writeLog(acc_data + "," + magValue);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void createFiles() {
        Log.d(TAG, "createFiles: ");
        try {
            sessionCSV = new File(getExternalFilesDir(null), SESSION_LOG_CSV);
            if (sessionCSV.exists()) {
                sessionCSV.delete();
                sessionCSV = new File(getExternalFilesDir(null), SESSION_LOG_CSV);
                Log.d(TAG, "createFiles: " + sessionCSV.createNewFile());
            }
            mFileWriter = new FileWriter(sessionCSV, true);
        } catch (Exception e) {
            Toast.makeText(this, "writer error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void registerAvailableSensors() {
        sensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        //logAvailableSensors();

        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            /*gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            hearRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);*/

            if (accelerometerSensor != null) {
                sensorManager.registerListener(this, accelerometerSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }

            /*if (gyroscopeSensor != null) {
                sensorManager.registerListener(this, gyroscopeSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }

            if (hearRateSensor != null) {
                sensorManager.registerListener(this, gyroscopeSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }*/
        }
    }

    private void unregisterAvailableSensors() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private void writeLog(String data) {
        try {
            mFileWriter = new FileWriter(sessionCSV, true);
            mFileWriter.append(data);
            mFileWriter.append("\n");
            mFileWriter.close();
            Log.d(TAG, "writeLog() Sensor Data: " + data);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IO Exception NN: " + e.toString());
        }
    }

    /**
     * Log all available sensors to logcat
     */
    private void logAvailableSensors() {
        Log.d(TAG, "logAvailableSensors: ");
        final List<Sensor> sensors = ((SensorManager) getSystemService(SENSOR_SERVICE))
                .getSensorList(Sensor.TYPE_ALL);
        Log.d(TAG, "=== LIST AVAILABLE SENSORS ===");
        Log.d(TAG, String.format(Locale.getDefault(), "|%-35s|%-38s|%-6s|",
                "SensorName", "StringType", "Type"));
        for (Sensor sensor : sensors) {
            Log.v(TAG, String.format(Locale.getDefault(), "|%-35s|%-38s|%-6s|",
                    sensor.getName(), sensor.getStringType(), sensor.getType()));
        }
        Log.d(TAG, "=== LIST AVAILABLE SENSORS ===");
    }

    private double togglePrecision(float d) {
        return Math.round(d * p) / pd;
    }
}
