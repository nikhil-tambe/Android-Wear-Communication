package com.nikhil.wear.sensors;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.compat.BuildConfig;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.nikhil.shared.Constants.MathC.p;
import static com.nikhil.shared.Constants.MathC.pd;

/**
 * Created by Nikhil on 24/7/17.
 */

public class SensorService extends Service implements SensorEventListener {

    public static final String TAG = "nikhil " + SensorService.class.getSimpleName();
    SensorManager sensorManager;
    Sensor accelerometerSensor, gyroscopeSensor, hearRateSensor;
    SendSensorData sendSensorData;
    String acc_data = "0,0,0";
    String gyro_data = "0,0,0";
    String hr_data = "72.0";


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        sendSensorData = SendSensorData.getInstance(this);
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

        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return; // If sensor is unreliable, then just return
        }
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acc_data = togglePrecision(event.values[0]) + ","
                    + togglePrecision(event.values[1]) + ","
                    + togglePrecision(event.values[2]);
        }
        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyro_data = togglePrecision(event.values[0]) + ","
                    + togglePrecision(event.values[1]) + ","
                    + togglePrecision(event.values[2]);
        }
        if (sensor.getType() == Sensor.TYPE_HEART_RATE) {
            hr_data = Float.toString(event.values[0]);
        }

        String[] data = new String[] {acc_data, gyro_data , hr_data};

        sendSensorData.sendData(
                event.sensor.getType(),
                event.accuracy,
                event.timestamp,
                data);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void registerAvailableSensors() {
        sensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        //logAvailableSensors();

        if (sensorManager != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            hearRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

            if (accelerometerSensor != null) {
                sensorManager.registerListener(this, accelerometerSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }

            if (gyroscopeSensor != null) {
                sensorManager.registerListener(this, gyroscopeSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }

            if (hearRateSensor != null) {
                sensorManager.registerListener(this, gyroscopeSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private void unregisterAvailableSensors() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
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
